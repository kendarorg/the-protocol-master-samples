## The Protocol Master Sample

![](protocolmaster_s.gif)

This is a simple environment to test [The Protocol Master](https://github.com/kendarorg/the-protocol-master)
application. We will mock without a line of code the database and a rest api of a simple TODO web application.
(PS tanks [patrick-baker](https://github.com/patrick-baker/to-do-list) for the UI!)

Just for fun this is a .NET Core application with Entity Framework 
and very basic JQuery-based Front-End

* The documentation for the protocol master is [here](https://github.com/kendarorg/the-protocol-master)

## Prerequisites

* A Docker environment
* The Docker environment address (let's use DOCKER_SERVER for now)

### Machine Dependant Stuffs! Beware

All .NET applications run out of the box only on Intel architecture, that means

* Platform: linux/amd64
* Target Architecture: amd64

For Apple M processors should work the following, change the [HttpServer](HttpServer/Dockerfile)
and [RestServer](RestServer/Dockerfile) dockerfile according to your needs (see on
[Microsoft](https://devblogs.microsoft.com/dotnet/improving-multiplatform-container-support/) for
further info on the subject)

* Platform: linux/arm64
* Target Architecture: arm64

## What to do

If you have Intellij or know what are *.http files you can configure [this](net-core/someutilities.http) setting the myhost
variable to ```DOCKER_SERVER```

### Startup

* Configure your browser to use DOCKER_SERVER:29000 as HTTP/S Proxy
* Navigate to the "net-core" directory
* Run ```docker-compose up``` to generate the environment
* Several containers will be created
  * net_core_tpm: The Protocol Master server
  * db_mysql: The mysql database
  * net_core_http: The web-ui
  * net_core_rest: The rest back-end
* Download the SSL certificate from http://DOCKER_SERVER:28081/api/protocols/http-01/plugins/ssl-plugin/der and install it as a trusted root certificate

You can check now the application navigating (in the proxied browser) to anything 
you want, and you will se the calls flowing on the console. 

You can even try the application on [http://net_core_http/index.html](http://net_core_http/index.html)
but for the sake of simplicity please delete all tasks before continuing the tutorial

### Recording

* Start the recording on all protocols http://DOCKER_SERVER:28081/api/protocols/*/plugins/record-plugin/start
* Navigate to [http://net_core_http/index.html](http://net_core_http/index.html)
* Insert a new task and click Submit
  * Task Name: Laundry
  * Priority: High
  * Notes: Separate Colors
* Set the status to Completed 
* Click Update
* Click Archive
* Go on Archive tab (upper right) and notice the task
* Stop the recording on all protocols http://DOCKER_SERVER:28081/api/protocols/*/plugins/record-plugin/stop
* You can download all the recordings! http://DOCKER_SERVER:28081/api/storage/download as a zip file

### Look Ma, NO DATABASE

* Stop the ```db_mysql``` container
* Start the replaying on MySQL http://DOCKER_SERVER:28081/api/protocols/mysql-01/plugins/replay-plugin/start
* Refresh the page [http://net_core_http/index.html](http://net_core_http/index.html)
* Redo exactly all the actions
* And everything will work!!!! But with a fake DB
* Stop all the replayings http://DOCKER_SERVER:28081/api/protocols/*/plugins/replay-plugin/stop

### Look Ma, NOT EVEN THE API SERVER

* Stop the ```net_core_rest``` container
* Start the replaying on MySQL [http://net_core_tpm:8081/api/protocols/http-01/plugins/replay-plugin/start](http://net_core_tpm:8081/api/protocols/http-01/plugins/replay-plugin/start)
* Refresh the page [http://net_core_http/index.html](http://net_core_http/index.html)
* Redo exactly all the actions
* And everything will work!!!! But with a fake Rest API!!
* Stop all the replayings http://DOCKER_SERVER:28081/api/protocols/*/plugins/replay-plugin/stop

## If you like it Buy me a coffe :)

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/paypalme/kendarorg/1)
