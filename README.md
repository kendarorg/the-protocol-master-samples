## The Protocol Master Proxy Samples

![](protocolmaster_s.gif)

### Table of Contents

* <a href="#nme">Simple to-do app</a> (.net-core, mysql, EF)
* <a href="#jmm">Java Quotations app</a> (java, mysql, mqtt)
* <a href="#pma">Python Quotations app</a> (python, mysql, amqp-091)
* <a href="#gca">Golang Chat app</a> (golang, postgres, redis)
* <a href="#mitms">Intercept Phone</a> with Wireguard

These are simple environments to test [The Protocol Master](https://github.com/kendarorg/the-protocol-master)
application. We will mock without a line of code the database and a rest api of a simple TODO web application.

* The documentation for the protocol master is [here](https://github.com/kendarorg/the-protocol-master)

### If you like it Buy me a coffee :)

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/paypalme/kendarorg/1)

<br>

## Prerequisites

* A Docker environment
* The Docker environment address (let's use DOCKER_SERVER for now)
* To play with the applications as stand-alone you can replace all 192.168.131.20 addresses (my docker server) with your
  docker server address
* All needed docker images loaded (if you wanna pre-setup everything)

`
docker image pull mysql:8
docker image pull mcr.microsoft.com/dotnet/aspnet:8.0
docker image pull amazoncorretto:17.0.7-alpine
docker image pull docker.io/library/node:18-alpine
docker image pull docker.io/library/node:lts-alpine
docker image pull eclipse-mosquitto:2.0.20
docker image pull adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine-slim
docker image pull postgres:9.6.18
docker image pull golang:1.18-bullseye
docker image pull python:3.9.13
`

### Automatic testing

The project `net-core` can be run automatically with selenium and testcontainers starting the tests inside the `e2etest`
project. If no env variable is set, the Chrome browser will run till the end of the test. Two environment variables can
be used

* RUN_VISIBLE: Show the browser
* HUMAN_DRIVEN: Show the browser AND show blocking messages in the various phases

### Machine Dependant Stuffs! Beware

All .NET applications run out of the box only on Intel architecture, that means

* Platform: linux/amd64
* Target Architecture: amd64

For Apple M processors should work the following, change the [HttpServer](net-core/HttpServer/Dockerfile)
and [RestServer](net-core/RestServer/Dockerfile) dockerfile according to your needs (see on
[Microsoft](https://devblogs.microsoft.com/dotnet/improving-multiplatform-container-support/) for
further info on the subject)

* Platform: linux/arm64
* Target Architecture: arm64

<a id="nme"></a>

## Simple TODO app (mysql,http,.NET)

(Thanks [patrick-baker](https://github.com/patrick-baker/to-do-list) for the UI!)

If you have Intellij or know what are *.http files you can configure [this](net-core/someutilities.http) setting the
`myhost` variable to ```DOCKER_SERVER```

### Startup

* Clone the repo ```https://github.com/kendarorg/the-protocol-master-samples.git```
* Configure your browser to use DOCKER_SERVER:29000 as HTTP/S Proxy
* Navigate to the "net-core" directory
* Run ```docker-compose up``` to generate the environment
* Several containers will be created
    * net-core-tpm: The Protocol Master server
    * net-core-mysql: The mysql database
    * net-core-http: The web-ui
    * net-core-rest: The rest back-end
* Download the SSL certificate
  from [http://net-core-tpm:8081/api/protocols/http-01/plugins/ssl-plugin/der](http://net-core-tpm:8081/api/protocols/http-01/plugins/ssl-plugin/der)
  and install
  it as a trusted root certificate

You can check now the application navigating (in the proxied browser) to anything
you want, and you will se the calls flowing on the console.

You can even try the application on [http://net-core-http/index.html](http://net-core-http/index.html)
but for the sake of simplicity please delete all tasks before continuing the tutorial

### Recording

* Start the recording on all
  protocols [http://net-core-tpm:8081/api/protocols/all/plugins/record-plugin/start](http://net-core-tpm:8081/api/protocols/all/plugins/record-plugin/start)
* Navigate to [http://net-core-http/index.html](http://net-core-http/index.html)
* Insert a new task and click Submit
    * Task Name: Laundry
    * Priority: High
    * Notes: Separate Colors
* Set the status to Completed
* Click Update
* Click Archive
* Go on Archive tab (upper right) and notice the task
* Stop the recording on all
  protocols [http://net-core-tpm:8081/api/protocols/all/plugins/record-plugin/stop](http://net-core-tpm:8081/api/protocols/all/plugins/record-plugin/stop)
* You can download all the
  recordings! [http://net-core-tpm:8081/api/storage/download](http://net-core-tpm:8081/api/storage/download) as a zip
  file

### Look Ma, NO DATABASE

* Stop the ```db_mysql``` container
* Start the replaying on
  MySQL [http://net-core-tpm:8081/api/protocols/mysql-01/plugins/replay-plugin/start](http://net-core-tpm:8081/api/protocols/mysql-01/plugins/replay-plugin/start)
* Refresh the page [http://net-core-http/index.html](http://net-core-http/index.html)
* Redo exactly all the actions
* And everything will work!!!! But with a fake DB
* Stop all the
  replaying [http://net-core-tpm:8081/api/protocols/all/plugins/replay-plugin/stop](http://net-core-tpm:8081/api/protocols/all/plugins/replay-plugin/stop)

### Look Ma, NOT EVEN THE API SERVER

* Stop the ```net-core-rest``` container
* Start the replaying on
  MySQL [http://net-core-tpm:8081/api/protocols/http-01/plugins/replay-plugin/start](http://net-core-tpm:8081/api/protocols/http-01/plugins/replay-plugin/start)
* Refresh the page [http://net-core-http/index.html](http://net-core-http/index.html)
* Redo exactly all the actions
* And everything will work!!!! But with a fake Rest API!!
* Stop all the
  replaying [http://net-core-tpm:8081/api/protocols/all/plugins/replay-plugin/stop](http://net-core-tpm:8081/api/protocols/all/plugins/replay-plugin/stop)

<a id="jmm"></a>

## Simple quotes app (java,mysql,mqtt)

If you have Intellij or know what are *.http files you can configure [this](net-core/someutilities.http) setting the
`myhost` variable to ```DOCKER_SERVER```

You can check the quotations going to ```http:\\java-rest\index.html```

### Startup

* Clone the repo ```https://github.com/kendarorg/the-protocol-master-samples.git```
* Configure your browser to use DOCKER_SERVER:29000 as HTTP/S Proxy
* Navigate to the "java" directory
* Run ```docker-compose up``` to generate the environment
* Several containers will be created
    * java-tpm: The Protocol Master server
    * java-mysql: The mysql database
    * java-mosquitto: The mqtt broker
    * java-rest: The application reading mqtt messages (and showing on [APIs](java-rest/swagger-ui/index.html))
    * java-quote-generator: The quote generation (every 10 seconds random stock quotes)
* Download the SSL certificate
  from [http://java-tpm:8081/api/protocols/http-01/plugins/ssl-plugin/der](http://java-tpm:8081/api/protocols/http-01/plugins/ssl-plugin/der)
  and install
  it as a trusted root certificate
* Connect your mysql ui to ```DOCKER_HOST:23306``` and use the database ```db```

Now your environment is ready for a real test!

### Recording

* Start the recording on mqtt-01
  protocol [http://java-tpm:8081/api/protocols/mqtt-01/plugins/record-plugin/start](http://java-tpm:8081/api/protocols/mqtt-01/plugins/record-plugin/start)
* Delete all records on ```db.quotations``` table
* Wait for some data on ```quotations``` table (at least 10 seconds, this is the "run-time")
* Stop the recording on all
  protocols [http://java-tpm:8081/api/protocols/all/plugins/record-plugin/stop](http://java-tpm:8081/api/protocols/all/plugins/record-plugin/stop)
* You can download all the
  recordings! [http://java-tpm:8081/api/storage/download](http://java-tpm:8081/api/storage/download) as a zip file

### Look Ma, NO BROKER

* Stop the ```java-quote-generation``` container
* Stop the ```java-rest``` container
* Delete all data on ```quotations``` table
* Start the replaying on
  MQTT [http://java-tpm:8081/api/protocols/mqtt-01/plugins/replay-plugin/start](http://java-tpm:8081/api/protocols/mqtt-01/plugins/replay-plugin/start)
* Check the new data on ```quotations``` table
* Mqtt simulation... done!
* Stop the replaying on all
  protocols [http://java-tpm:8081/api/protocols/all/plugins/replay-plugin/stop](http://java-tpm:8081/api/protocols/all/plugins/record-plugin/stop)

### Faking a message

* Activate the publish plugin
  MQTT [http://java-tpm:8081/api/protocols/mqtt-01/plugins/publish-plugin/start](http://java-tpm:8081/api/protocols/mqtt-01/plugins/publish-plugin/start)
* Restart Mosquitto
* Go to the swagger
  instance [http://java-tpm:8081/swagger-ui/index.html#/](http://java-tpm:8081/swagger-ui/index.html#/)
* Open
  the [connections](http://java-tpm:8081/swagger-ui/index.html#/plugins%2Fmqtt%2Fmqtt-01/get_api_protocols_mqtt_01_plugins_publish_plugin_connections)
  API and check the active connections
* Open
  the [message sending](http://java-tpm:8081/swagger-ui/index.html#/plugins%2Fmqtt%2Fmqtt-01/post_api_protocols_mqtt_01_plugins_publish_plugin_connections__connectionId___topic_)
  API
* Insert the following
    * connectionId: -1 (all the subscribed)
    * topic: quotations
    * Request body (the `unixtimestamp` must be in the future of nothing will appear :P )

<pre>
{
  "contentType": "text/plain",
  "body": "{ \"symbol\" : \"META\", \"date\" : [UNIXTIMESTAMP]999,\"price\" : 1000,  \"volume\" : 1000\n }"
}
</pre>

* Look on your message on the graph!

<a id="pma"></a>

## Simple quotes app (python,mysql,amqp)

If you have Intellij or know what are *.http files you can configure [this](net-core/someutilities.http) setting the
`myhost` variable to ```DOCKER_SERVER```

You can check the quotations going to ```http:\\py-rest\index.html```

### Startup

* Clone the repo ```https://github.com/kendarorg/the-protocol-master-samples.git```
* Configure your browser to use DOCKER_SERVER:29000 as HTTP/S Proxy
* Navigate to the "python" directory
* Run ```docker-compose up``` to generate the environment
* Several containers will be created
    * py-tpm: The Protocol Master server
    * py-mysql: The mysql database
    * py-rabbit: The amqp broker
    * py-rest: The application reading mqtt messages (and showing on [APIs](java-rest/swagger-ui/index.html))
    * py-quote-generator: The quote generation (every 10 seconds random stock quotes)
* Download the SSL certificate
  from [http://py-tpm:8081/api/protocols/http-01/plugins/ssl-plugin/der](http://py-tpm:8081/api/protocols/http-01/plugins/ssl-plugin/der)
  and install
  it as a trusted root certificate
* Connect your mysql ui to ```DOCKER_HOST:23306``` and use the database ```db```

Now your environment is ready for a real test!

### Recording

* Start the recording on mqtt-01
  protocol [http://py-tpm:8081/api/protocols/amqp-01/plugins/record-plugin/start](http://py-tpm:8081/api/protocols/amqp-01/plugins/record-plugin/start)
* Delete all records on ```db.quotations``` table
* Wait for some data on ```quotations``` table (at least 10 seconds, this is the "run-time")
* Stop the recording on all
  protocols [http://py-tpm:8081/api/protocols/all/plugins/record-plugin/stop](http://py-tpm:8081/api/protocols/all/plugins/record-plugin/stop)
* You can download all the
  recordings! [http://py-tpm:8081/api/storage/download](http://py-tpm:8081/api/storage/download) as a zip file

### Look Ma, NO BROKER

* Stop the ```py-quote-generation``` container
* Stop the ```py-rest``` container
* Delete all data on ```quotations``` table
* Start the replaying on
  MQTT [http://py-tpm:8081/api/protocols/amqp-01/plugins/replay-plugin/start](http://py-tpm:8081/api/protocols/amqp-01/plugins/replay-plugin/start)
* Check the new data on ```quotations``` table
* Mqtt simulation... done!
* Stop the replaying on all
  protocols [http://py-tpm:8081/api/protocols/all/plugins/replay-plugin/stop](http://py-tpm:8081/api/protocols/all/plugins/record-plugin/stop)

<a id="gca"></a>

## Golang Chat app (golang, postgres, redis)

### Startup

* Clone the repo ```https://github.com/kendarorg/the-protocol-master-samples.git```
* Configure your browser to use DOCKER_SERVER:29000 as HTTP/S Proxy
* Navigate to the "golang" directory
* Run ```docker-compose up``` to generate the environment
* Several containers will be created
    * go-tpm: The Protocol Master server
    * go-postgres: The postgres database
    * go-redis: The Redis server
    * go-rest: The chat application
* Download the SSL certificate
  from [http://go-tpm:8081/api/protocols/http-01/plugins/ssl-plugin/der](http://go-tpm:8081/api/protocols/http-01/plugins/ssl-plugin/der)
  and install
  it as a trusted root certificate
* Connect your postgres ui to ```DOCKER_HOST:25432``` and use the database ```db```

Now your environment is ready for a real test!

### Recording

* Start the recording on redis-01
  protocol [http://go-tpm:8081/api/protocols/redis-01/plugins/record-plugin/start](http://go-tpm:8081/api/protocols/redis-01/plugins/record-plugin/start)
* Open the proxied browser on [http://go-rest/index.html](http://go-rest/index.html) and login as "user1" on channel "
  common"
* Open another tab of the proxied browser on [http://go-rest/index.html](http://go-rest/index.html) and login as "user2"
  on channel "common"
* Write some messages on both browsers
* Stop the recording on all
  protocols [http://go-tpm:8081/api/protocols/all/plugins/record-plugin/stop](http://go-tpm:8081/api/protocols/all/plugins/record-plugin/stop)
* You can download all the
  recordings! [http://go-tpm:8081/api/storage/download](http://go-tpm:8081/api/storage/download) as a zip file

### Look Ma, NO BROKER

* Start the replaying on
  REDIS [http://go-tpm:8081/api/protocols/redis-01/plugins/replay-plugin/start](http://go-tpm:8081/api/protocols/redis-01/plugins/replay-plugin/start)
* Restart inserting data on the messages
* Stop the replaying on all
  protocols [http://go-tpm:8081/api/protocols/all/plugins/replay-plugin/stop](http://go-tpm:8081/api/protocols/all/plugins/record-plugin/stop)

### Fake messaging

* Open
  the [message sending](http://go-tpm:8081/swagger-ui/index.html#/plugins%2Fredis%2Fredis-01/post_api_protocols_redis_01_plugins_publish_plugin_connections__connectionId___topic_)
  API
* Insert the following
    * connectionId: -1 (all the subscribed)
    * channel: common
    * Request body

<pre>
{
  "contentType": "text/plain",
  "body": "fake: message"
}
</pre>

* Look on your message on the chat!

<a id="mitm"></a>

## Intercept Phone

This works well even when working with applications that do not support proxies

Both the Docker and your phone must be on the same network and visible to each other

* First replace in `mitm/docker-compose.yml` the `WG_HOST` variable with the one of your Docker Server
* Run `docker-compose up` in `mitm` directory
* Install the Wireguard client on your phone 
* Navigate to the Wireguard interface http://YOUR_DOCKER_SERVER:51821/
* And create a profile to install in Wireguard
* Now going on the report plugin http://YOUR_DOCKER_SERVER:28081/plugins/global/report-plugin
* You can query all DNS calls and see all requested domains
* Next step will be intercepting all this calls through TPM

`
SELECT(
  WHAT(cnt=COUNT(),tags.requestedDomain=tags.requestedDomain),
  GROUPBY(tags.requestedDomain),
  ORDERBY(DESC(cnt))
)
`
