# flightgear-control

Control and interact with a FlightGear aircraft generically using telnet and sockets. 

Easily and economically simulate a complex real-world machine with mechanical causality.

Generically fly aircraft by executing the plane's engine startup script and diligently correcting drift and deviation from a flight path by constantly repositioning the plane orientation within the simulator environment along a set of waypoints. 

#### Requirements ####

1. Linux x86_64 running with a windowing system.
1. Git
1. JDK 8
1. GPU preferably with more than 2Gb VRAM.

#### Setup ####

Documented [here](SETUP.md).

#### Building ####

Use tasks `jar`, `appjar`, and `sourcesjar` to generate the jars for this project.
* `jar` - The primary output jar for incorporating into other projects.
* `appjar` - Driver applications for testing functionality locally.
* `sourcesjar` - Sources jar to accompany the main jar for incorporating into other projects.

#### Flights ####

1. Sample main programs reside in packages: org.jason.fgcontrol.aircraft.[aircraft name].app, and can be executed as Java applications. Sample applications will run with a default configuration, or supplied properties files in the `scripts/conf` directory to specify simulator port ranges. 
