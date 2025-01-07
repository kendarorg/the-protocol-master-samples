package main

import (
	"context"
	"database/sql"
	"fmt"
	"github.com/go-redis/redis/v8"
	"github.com/gorilla/mux"
	"github.com/gorilla/websocket"
	_ "github.com/lib/pq"
	"gopkg.in/ini.v1"
	"log"
	"net/http"
	"os"
	"strconv"
	"strings"
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

	router.HandleFunc("/ws/{channel}", func(response http.ResponseWriter, request *http.Request) {
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
						CHANNEL=$1 ORDER BY id DESC`
	rows, err := db.Query(sqlStatement, channelToQuery)

	response.Header().Set("Content-Type", "application/json")
	response.WriteHeader(200)
	var toSend = `[`
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
	response.Write([]byte(toSend))
}

func handleWebSocket(response http.ResponseWriter, request *http.Request, dbSection *ini.Section, redisSection *ini.Section) {
	rdb := redis.NewClient(&redis.Options{
		Addr: redisSection.Key("host").String() + ":" + redisSection.Key("port").String(),
	})
	vars := mux.Vars(request)
	channel := vars["channel"]

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

	//Open db connection
	db, err := sql.Open("postgres", psqlInfo)
	if err != nil {
		panic(err)
	}
	defer db.Close()
	defer conn.Close()

	//Subscribe to channel
	sub := rdb.Subscribe(ctx, channel)
	defer sub.Close()
	ch := sub.Channel()

	go func() {
		for {
			//Read all messages
			_, msg, err := conn.ReadMessage()
			if err != nil {
				log.Println("Read error:", err)
				return
			}

			//And publish them
			strMsg := string(msg)
			if err := rdb.Publish(ctx, channel, strMsg).Err(); err != nil {
				log.Println("Publish error:", err)
				return
			}

			log.Println("Read Message :" + strMsg)

			//Save the data after publishing
			arrayString := strings.SplitN(strMsg, ":", 2)
			sqlStatement := `INSERT INTO messages (channel,sender, message)
						VALUES ($1, $2, $3)`
			_, err = db.Exec(sqlStatement, channel, arrayString[0], arrayString[1])
			if err != nil {
				panic(err)
			}
		}
	}()

	for msg := range ch {

		if err := conn.WriteMessage(websocket.TextMessage, []byte(msg.Payload)); err != nil {
			log.Println("Write error:", err)
			return
		}

		log.Println("Write Message :" + msg.Payload)
	}
}
