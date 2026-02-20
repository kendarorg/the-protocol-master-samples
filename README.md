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

### Running the applications

First run the start.sh/ps1 script in the root directory of the project to enable all scripts to run.

On the various applications you can find a start.sh/ps1 script that will start everything for you.
If the protocol master compiled source is in the directory parallel to the samples it will use
the jar from there.

You can also run the applications via the tests in the e2etest project.

### Notes for macOs Users

Should set the following on your .zprofile see [here](https://github.com/testcontainers/testcontainers-rs/pull/800):

```
export DOCKER_DEFAULT_PLATFORM=linux/arm64v8
export TESTCONTAINERS_RYUK_DISABLED=true
export DOCKER_HOST=tcp://localhost:2375
```

Check [here](https://podman-desktop.io/docs/migrating-from-docker/using-the-docker_host-environment-variable) for Podman


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
docker image pull mysql:8.0.40
docker image pull mcr.microsoft.com/dotnet/aspnet:8.0
docker image pull amazoncorretto:17.0.7-alpine
docker image pull docker.io/library/node:18-alpine
docker image pull docker.io/library/node:lts-alpine
docker image pull eclipse-mosquitto:2.0.20
docker image pull eclipse-temurin:25-jdk-alpine
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
* Download the DER SSL certificate (root certificate) from the [tpm host ui](http://net-core-tpm:8081/plugins/http-01/ssl-plugin?accordion=collapseSpecificPlugin)
  and install it as a trusted root certificate

You can check now the application navigating (in the proxied browser) to anything
you want, and you will se the calls flowing on the console.

You can even try the application on [http://net-core-http/index.html](http://net-core-http/index.html)
but for the sake of simplicity please delete all tasks before continuing the tutorial

### Recording

* Start all the recording-plugins [on the UI](http://net-core-tpm:8081/plugins?accordion=collapseWildcard)
* Navigate to [http://net-core-http/index.html](http://net-core-http/index.html)
* Insert a new task and click Submit
    * Task Name: Laundry
    * Priority: High
    * Notes: Separate Colors
* Set the status to Completed
* Click Update
* Click Archive
* Go on Archive tab (upper right) and notice the task
* Stop all the recording-plugins [on the UI](http://net-core-tpm:8081/plugins?accordion=collapseWildcard)
* You can download all the [on the ui](http://net-core-tpm:8081/storage) as a zip file

### Look Ma, NO DATABASE

* Stop the ```db_mysql``` container
* Start on MySQL the [replay-plugin](http://net-core-tpm:8081/plugins/mysql-01/replay-plugin)
* Refresh the page [http://net-core-http/index.html](http://net-core-http/index.html)
* Redo exactly all the actions
* And everything will work!!!! But with a fake DB
* Stop on MySQL the [replay-plugin](http://net-core-tpm:8081/plugins/mysql-01/replay-plugin)

### Look Ma, NOT EVEN THE API SERVER

* Stop the ```net-core-rest``` container
* Start on Http the [replay-plugin](http://net-core-tpm:8081/plugins/http-01/replay-plugin)
* Refresh the page [http://net-core-http/index.html](http://net-core-http/index.html)
* Redo exactly all the actions
* And everything will work!!!! But with a fake Rest API!!
* Stop on Http the [replay-plugin](http://net-core-tpm:8081/plugins/http-01/replay-plugin)

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
    * java-rest: The application reading mqtt messages (and showing on [APIs](http://java-rest/swagger-ui/index.html))
    * java-quote-generator: The quote generation (every 10 seconds random stock quotes)
* Download the DER SSL certificate (root certificate) from the [tpm host ui](http://java-tpm:8081/plugins/http-01/ssl-plugin?accordion=collapseSpecificPlugin)
  and install it as a trusted root certificate
* Connect your mysql ui to ```DOCKER_HOST:23306``` and use the database ```db```

Now your environment is ready for a real test!

### Recording

* Start on Mqtt the [record-plugin](http://java-tpm:8081/plugins/mqtt-01/record-plugin)
* Delete all records on ```db.quotations``` table
* Wait for some data on ```quotations``` table (at least 10 seconds, this is the "run-time")
* Stop on Mqtt the [record-plugin](http://java-tpm:8081/plugins/mqtt-01/record-plugin)
* You can download all the [on the ui](http://java-tpm:8081/storage) as a zip file

### Look Ma, NO BROKER

* Stop the ```java-quote-generation``` container
* Stop the ```java-rest``` container
* Delete all data on ```quotations``` table
* Refresh all the open pages for `java-rest`
* Start on Mqtt the [replay-plugin](http://java-tpm:8081/plugins/mqtt-01/replay-plugin)
* Check the new data on ```quotations``` table or look on the chart the values updating
* Mqtt simulation... done!
* Start on Mqtt the [replay-plugin](http://java-tpm:8081/plugins/mqtt-01/replay-plugin)

### Faking a message

* Activate the Mqtt  [publish-plugin](http://java-tpm:8081/plugins/mqtt-01/publish-plugin)
* Restart Mosquitto
* Open the publish plugin "[Publish Section](http://java-tpm:8081/plugins/mqtt-01/publish-plugin?accordion=collapseSpecificPlugin)"
* Set the `content-type` to `JSON`
* Topic to `quotations`
* Message body to the following with the `UNIXTIMESTAMP` being current time in milliseconds

```
{ "symbol" : "META","date" : 1744030940999,"price" : 1000.00,  "volume" : 1000 }
```

* Look on your message on the graph! Here it is your fake message

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
* Download the DER SSL certificate (root certificate) from the [tpm host ui](http://py-tpm:8081/plugins/http-01/ssl-plugin?accordion=collapseSpecificPlugin)
  and install it as a trusted root certificate
* Connect your mysql ui to ```DOCKER_HOST:23306``` and use the database ```db```

Now your environment is ready for a real test!

### Recording

* Start on Amqp the [record-plugin](http://py-tpm:8081/plugins/amqp-01/record-plugin)
* Delete all records on ```db.quotations``` table
* Wait for some data on ```quotations``` table (at least 10 seconds, this is the "run-time")
* Stop on Amqp the [record-plugin](http://py-tpm:8081/plugins/amqp-01/record-plugin)
* You can download all the [on the ui](http://py-tpm:8081/storage) as a zip file

### Look Ma, NO BROKER

* Stop the ```py-quote-generation``` container
* Stop the ```py-rest``` container
* Delete all data on ```quotations``` table
* Refresh all the open pages for `py-rest`
* Start on Amqp the [replay-plugin](http://py-tpm:8081/plugins/amqp-01/replay-plugin)
* Check the new data on ```quotations``` table or look on the chart the values updating 
* Amqp simulation... done!
* Stop on Amqp the [replay-plugin](http://py-tpm:8081/plugins/amqp-01/replay-plugin)

### Faking a message

* Activate the Amqp  [publish-plugin](http://py-tpm:8081/plugins/amqp-01/publish-plugin)
* Restart Mosquitto
* Open the publish plugin "[Publish Section](http://py-tpm:8081/plugins/amqp-01/publish-plugin?accordion=collapseSpecificPlugin)"
* Set the connection to "ALL"
* Set the `app-id` to `test`
* Queue to `quotations`
* Exchange to `stock`
* Message body to the following with a time after "now"

```
{ "symbol" : "META","date" :"2025-04-07 15:11:11","price" : 1000.1,  "volume" : 1000 }
```

* Look on your message on the graph! Here it is your fake message


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
* Download the DER SSL certificate (root certificate) from the [tpm host ui](http://go-tpm:8081/plugins/http-01/ssl-plugin?accordion=collapseSpecificPlugin)
  and install it as a trusted root certificate
* Connect your postgres ui to ```DOCKER_HOST:25432``` and use the database ```db```

Now your environment is ready for a real test!

### Recording

* Start on Redis the [record-plugin](http://go-tpm:8081/plugins/redis-01/record-plugin)
* Open the proxied browser on [http://go-rest/index.html](http://go-rest/index.html) and login as "user1" on channel "
  common"
* Open another tab of the proxied browser on [http://go-rest/index.html](http://go-rest/index.html) and login as "user2"
  on channel "common"
* Write some messages on both browsers
* Stop on Redis the [record-plugin](http://go-tpm:8081/plugins/redis-01/record-plugin)
* You can download all the [on the ui](http://go-tpm:8081/storage) as a zip file

### Look Ma, NO BROKER

* Start on Redis the [replay-plugin](http://go-tpm:8081/plugins/redis-01/replay-plugin)
* Restart inserting data on the messages
* Stop on Redis the [replay-plugin](http://go-tpm:8081/plugins/redis-01/replay-plugin)


### Faking a message

* Activate the Redis  [publish-plugin](http://go-tpm:8081/plugins/redis-01/publish-plugin)
* Open the publish plugin "[Publish Section](http://py-tpm:8081/plugins/redis-01/publish-plugin?accordion=collapseSpecificPlugin)"
* Set the `content-type` to `JSON`
* Queue to `common`
* Message body to `fake: message`
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

```
SELECT(
  WHAT(cnt=COUNT(),tags.requestedDomain=tags.requestedDomain),
  GROUPBY(tags.requestedDomain),
  ORDERBY(DESC(cnt))
)
```
