# flightgear-control
Control and interact with a FlightGear aircraft generically with telnet and sockets. 

Generically fly by starting the plane's engine and diligently correcting drift and deviation from a flight path by constantly repositioning the plane within the sim environment. 

#### Setup ####

1. Build flightgear directly from source or install the package through your distro's package manager.
1. Run flightgear and verify that models "c172p" and "org.flightgear.fgaddon.stable_2018.f15c". Install the models if they're missing.
1. Copy the protocol xml files in the protocol directory of this project to flightgear's Protocol directory. Copy the files themselves and not the directory structure. 
    `cp -v protocol/*/*/*.xml /path/to/flightgear/Protocol`
1. Run the scripts in the scripts directory to launch the flightgear simulator with the required configuration for control. Check that the hosts and ports set for the sockets jive with what's already running, and that the flightgear binary fgfs is on the system path.
1. By default, telemetry will be output on port 6501. Netcat can be used to view the data directly with `nc -l -u -p 6501 127.0.0.1`.
1. Import this project into your IDE as a gradle project.
1. Generate the project jar using the gradle jar task for use/integration in other projects. 

#### Flights ####

1. Sample main programs reside in packages: org.jason.fgcontrol.aircraft.[aircraft name].app, and can be executed with a simple Java run configuration.

