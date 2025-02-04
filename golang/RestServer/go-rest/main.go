package main

import (
	"context"
	"database/sql"
	"fmt"
	"github.com/gomodule/redigo/redis"
	"github.com/gorilla/mux"
	"github.com/gorilla/websocket"
	_ "github.com/lib/pq"
	"gopkg.in/ini.v1"
	"log"
	"net/http"
	"os"
	"strconv"
	"strings"
	"time"
)

var (
	ctx      = context.Background()
	upgrader = websocket.Upgrader{
		CheckOrigin: func(r *http.Request) bool {
			return true
		},
	}
)

func main() {
	iniData, err := ini.Load("properties.ini")
	if err != nil {
		fmt.Printf("Fail to read file: %v", err)
		os.Exit(1)
	}
	dbSection := iniData.Section("database")
	redisSection := iniData.Section("redis")
	mainSection := iniData.Section("main")

	router := mux.NewRouter()

	router.HandleFunc("/api/{channel}", func(response http.ResponseWriter, request *http.Request) {
		handleInit(response, request, dbSection)
	})

	router.HandleFunc("/api/status", func(response http.ResponseWriter, request *http.Request) {
		handleStatus(response, request)
	})

	router.HandleFunc("/ws/{channel}/{user}", func(response http.ResponseWriter, request *http.Request) {
		handleWebSocket(response, request, dbSection, redisSection)
	})

	fileServer := http.FileServer(http.Dir("./web"))
	router.PathPrefix("/").Handler(fileServer)

	port, err := strconv.Atoi(mainSection.Key("port").String())

	log.Println("Server started on :" + strconv.Itoa(port))
	http.ListenAndServe(":"+strconv.Itoa(port), router)
}

func handleStatus(response http.ResponseWriter, request *http.Request) {
	response.Header().Add("Content-Type", "text/plain")
	response.WriteHeader(200)
	response.Write([]byte("OK"))
}

func handleInit(response http.ResponseWriter, request *http.Request, dbSection *ini.Section) {
	psqlInfo := fmt.Sprintf("host=%s port=%s user=%s "+
		"password=%s dbname=%s sslmode=disable",
		dbSection.Key("host").String(),
		dbSection.Key("port").String(),
		dbSection.Key("user").String(),
		dbSection.Key("password").String(),
		dbSection.Key("db").String())
	db, err := sql.Open("postgres", psqlInfo)
	if err != nil {
		panic(err)
	}
	vars := mux.Vars(request)
	channelToQuery := vars["channel"]

	defer db.Close()
	sqlStatement := `SELECT * FROM messages WHERE
						CHANNEL=$1 ORDER BY id ASC`
	rows, err := db.Query(sqlStatement, channelToQuery)

	response.Header().Set("Content-Type", "application/json")
	response.WriteHeader(200)
	var toSend = `[`
	if rows == nil {
		toSend = toSend + `]`
	} else {

		counter := 0
		// Loop through rows, using Scan to assign column data to struct fields.
		for rows.Next() {
			var id int
			var channel string
			var sender string
			var message string
			if err := rows.Scan(&id, &channel, &sender,
				&message); err != nil {
			}
			if counter > 0 {
				toSend = toSend + `,`
			}
			toSend = toSend +
				`{"id":"` +
				strconv.Itoa(id) +
				`","sender":"` +
				sender +
				`","message":"` +
				message + `"}`
			counter++
		}
		toSend = toSend + `]`
	}
	response.Write([]byte(toSend))
}

func subscribe(conn *websocket.Conn, channelName string, redisSection *ini.Section) error {
	// Connect to Redis
	c, err := redis.Dial("tcp", redisSection.Key("host").String()+":"+redisSection.Key("port").String())
	if err != nil {
		return fmt.Errorf("failed to connect to Redis: %w", err)
	}
	defer c.Close()

	// Create a new PubSub connection
	psc := redis.PubSubConn{Conn: c}
	err = psc.Subscribe(channelName)
	if err != nil {
		return fmt.Errorf("failed to subscribe: %w", err)
	}
	log.Println("Subscribed to channel:", channelName)

	// Listen for messages
	for {
		switch v := psc.Receive().(type) {
		case redis.Message:
			log.Println("Receiving Message :" + string(v.Data))
			if err := conn.WriteMessage(websocket.TextMessage, v.Data); err != nil {
				log.Println("Write error:", err)
				break
			}

			log.Println("Write Message :" + string(v.Data))
		case redis.Subscription:
			log.Printf("Subscription message: %s to %s with count %d", v.Kind, v.Channel, v.Count)
		case error:
			log.Printf("Connection error: %v", v)
			return v // Triggers reconnection
		}
	}
}

type messageToParse struct {
	message string
	sent    bool
}

var connectionsMap = make(map[string]messageToParse)

func handleWebSocket(response http.ResponseWriter, request *http.Request, dbSection *ini.Section, redisSection *ini.Section) {

	vars := mux.Vars(request)
	channel := vars["channel"]
	username := vars["username"]

	//Upgrade to websocket
	conn, err := upgrader.Upgrade(response, request, nil)
	if err != nil {
		log.Println(err)
		return
	}
	psqlInfo := fmt.Sprintf("host=%s port=%s user=%s "+
		"password=%s dbname=%s sslmode=disable",
		dbSection.Key("host").String(),
		dbSection.Key("port").String(),
		dbSection.Key("user").String(),
		dbSection.Key("password").String(),
		dbSection.Key("db").String())

	go func() {
		_, ok := connectionsMap[username]
		if !ok {
			connectionsMap[username] = messageToParse{
				message: "",
				sent:    false,
			}
		} else {
			toPrint, _ := connectionsMap[username]
			log.Printf("Status unsent " + toPrint.message + " " + strconv.FormatBool(toPrint.sent))
		}
		connectionData, ok := connectionsMap[username]
		for {
			log.Printf("Reconnecting")
			rdb, err := redis.Dial("tcp", redisSection.Key("host").String()+":"+redisSection.Key("port").String())
			if err != nil {
				log.Printf("Subscription failed: %v. Reconnecting...", err)
				time.Sleep(200 * time.Millisecond)
				continue
			}
			db, err := sql.Open("postgres", psqlInfo)
			if err != nil {
				log.Printf("Subscription failed: %v. Reconnecting...", err)
				time.Sleep(200 * time.Millisecond)
				rdb.Close()
				continue
			}
			for {
				if connectionData.message == "" {
					//Read all messages
					_, msg, err := conn.ReadMessage()
					if err != nil {
						conn.Close()
						rdb.Close()
						db.Close()
						log.Println("Read error:", err)
						time.Sleep(200 * time.Millisecond)
						return
					}
					connectionData.message = string(msg)
				}

				if connectionData.sent == false {
					//And publish them
					err = rdb.Send("PUBLISH", channel, connectionData.message)
					if err != nil {
						rdb.Close()
						db.Close()
						log.Println("Send error 1:", err)
						time.Sleep(200 * time.Millisecond)
						break
					}
					err = rdb.Flush()
					if err != nil {
						rdb.Close()
						db.Close()
						log.Println("Send error 2:", err)
						time.Sleep(200 * time.Millisecond)
						break
					}
					_, err := rdb.Receive()
					if err != nil {
						rdb.Close()
						db.Close()
						log.Println("Send error 3:", err)
						time.Sleep(200 * time.Millisecond)
						break
					}

					log.Println("Sent:", connectionData.message)
					connectionData.sent = true
				}

				//Save the data after publishing
				arrayString := strings.SplitN(connectionData.message, ":", 2)
				sqlStatement := `INSERT INTO messages (channel,sender, message)
						VALUES ($1, $2, $3)`
				_, err = db.Exec(sqlStatement, channel, arrayString[0], arrayString[1])
				if err != nil {
					rdb.Close()
					db.Close()
					log.Println("Db error:", err)
					break
				}
				log.Println("Stored on Db")
				connectionData.sent = false
				connectionData.message = ""
			}
		}

	}()

	for {
		err := subscribe(conn, channel, redisSection)
		if err != nil {
			log.Printf("Subscription failed: %v. Reconnecting...", err)
			time.Sleep(2 * time.Second) // Delay before retrying
		}
	}
}
