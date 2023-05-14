# flightgear-control

Control and interact with a FlightGear aircraft generically using telnet and sockets. 

Easily and economically simulate a complex real-world machine with mechanical causality.

Generically fly aircraft by executing the plane's engine startup script and diligently correcting drift and deviation from a flight path by constantly repositioning the plane orientation within the simulator environment along a set of waypoints. 


  
<video controls="controls" muted="muted" loop="loop" autoplay="autoplay" src="https://github.com/jas0ndiamond/flightgear-control/assets/7103526/1934b311-00de-4316-be74-1b0301427464" >
  Your browser does not support the video tag.
</video>

----



#### Requirements ####

1. Linux x86_64 running with a windowing system.
1. Git
1. JDK 8
1. GPU preferably with more than 2Gb VRAM.

----
#### Setup ####

Documented [here](doc/SETUP.md).

----
#### Building ####

Use tasks `jar`, `appjar`, and `sourcesjar` to generate the jars for this project.
* `jar` - The primary output jar for incorporating into other projects.
* `appjar` - Driver applications for testing functionality locally.
* `sourcesjar` - Sources jar to accompany the main jar for incorporating into other projects.

----
#### Running Flights ####

Documented [here](doc/OPERATION.md).
