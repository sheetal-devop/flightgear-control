#!/bin/bash

fgfs \
 --prop:/nasal/local_weather/enabled=false\
 --metar=XXXX 012345Z 15003KT 12SM SCT041 FEW200 20/08 Q1015 NOSIG\
 --prop:/environment/weather-scenario=Fair weather\
 --timeofday=noon\
 --disable-rembrandt\
 --enable-terrasync\
 --aircraft=org.flightgear.fgaddon.stable_2018.747-400\
 --fg-scenery=/usr/share/games/flightgear/Scenery\
 --fg-aircraft=/usr/share/games/flightgear/Aircraft\
 --airport=PHNL\
 --httpd=5500\
 --telnet=5501\
 --disable-sound\
 --allow-nasal-from-sockets

