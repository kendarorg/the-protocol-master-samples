package main

import (
	"context"
	"database/sql"
	"fmt"
	"gopkg.in/ini.v1"
	"log"
	"net/http"
	"os"
	"strings"

	"github.com/go-redis/redis/v8"
	"github.com/gorilla/mux"
	"github.com/gorilla/websocket"
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

	r := mux.NewRouter()
	r.HandleFunc("/ws/{channel}", func(w http.ResponseWriter, r *http.Request) {
		handleWebSocket(w, r, dbSection, redisSection)
	})

	r.HandleFunc("/ws/{channel}/{user}", func(w http.ResponseWriter, r *http.Request) {
		handleInit(w, r, dbSection)
	})

	r.HandleFunc("/api/status", func(w http.ResponseWriter, r *http.Request) {
		handleStatus(w, r)
	})

	fs := http.FileServer(http.Dir("./web"))
	r.PathPrefix("/").Handler(fs)

	log.Println("Server started on :" + mainSection.Key("port").String())
	log.Fatal(http.ListenAndServe(":"+mainSection.Key("port").String(), r))
}

func handleStatus(w http.ResponseWriter, r *http.Request) {
	w.Write([]byte("OK"))
	w.WriteHeader(200)
	w.Header().Add("Content-Type", "text/plain")
}

func handleInit(w http.ResponseWriter, r *http.Request, dbSection *ini.Section) {
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
	vars := mux.Vars(r)
	channelToQuery := vars["channel"]

	defer db.Close()
	sqlStatement := `SELECT * FROM messages WHERE
						CHANNEL=$1 ORDER BY id DESC`
	rows, err := db.Query(sqlStatement, channelToQuery)

	w.Write([]byte("["))
	counter := 0
	// Loop through rows, using Scan to assign column data to struct fields.
	for rows.Next() {
		var id int64
		var channel string
		var sender string
		var message string
		if err := rows.Scan(&id, &channel, &sender,
			&message); err != nil {
		}
		if counter > 0 {
			w.Write([]byte(","))
		}
		w.Write([]byte(
			"{\"id\":\"" +
				string(id) +
				"\",\"sender\":\"" +
				sender +
				"\",\"message\":\"" +
				message + "\"}"))
		counter++
	}

	w.Write([]byte("]"))
	w.WriteHeader(200)
	w.Header().Add("Content-Type", "application/json")
}

func handleWebSocket(w http.ResponseWriter, r *http.Request, dbSection *ini.Section, redisSection *ini.Section) {
	rdb := redis.NewClient(&redis.Options{
		Addr: redisSection.Key("host").String() + ":" + redisSection.Key("port").String(),
	})
	vars := mux.Vars(r)
	channel := vars["channel"]

	conn, err := upgrader.Upgrade(w, r, nil)
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
	db, err := sql.Open("postgres", psqlInfo)
	if err != nil {
		panic(err)
	}
	defer db.Close()
	defer conn.Close()

	sub := rdb.Subscribe(ctx, channel)
	defer sub.Close()
	ch := sub.Channel()

	go func() {
		for {
			_, msg, err := conn.ReadMessage()
			if err != nil {
				log.Println("Read error:", err)
				return
			}

			if err := rdb.Publish(ctx, channel, string(msg)).Err(); err != nil {
				log.Println("Publish error:", err)
				return
			}
		}
	}()

	for msg := range ch {
		arrayString := strings.SplitN(msg.Payload, ",", 2)
		sqlStatement := `INSERT INTO messages (channel,sender, message)
						VALUES ($1, $2, $3)`
		_, err = db.Exec(sqlStatement, channel, arrayString[0], arrayString[1])
		if err != nil {
			panic(err)
		}
		if err := conn.WriteMessage(websocket.TextMessage, []byte(msg.Payload)); err != nil {
			log.Println("Write error:", err)
			return
		}
	}
}
