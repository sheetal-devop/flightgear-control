#!/bin/bash

fgfs \
 --verbose\
 --prop:/nasal/local_weather/enabled=false\
 --metar=XXXX 012345Z 15003KT 12SM SCT041 FEW200 20/08 Q1015 NOSIG\
 --prop:/environment/weather-scenario=Fair weather\
 --timeofday=noon\
 --disable-rembrandt\
 --aircraft=org.flightgear.fgaddon.stable_2018.747-400\
 --fg-scenery=/usr/share/games/flightgear/Scenery\
 --fg-aircraft=/usr/share/games/flightgear/Aircraft\
 --airport=CYVR\
 --disable-sound\
 --disable-real-weather-fetch\
 --geometry=1024x768\
 --texture-filtering=4\
 --disable-anti-alias-hud\
 --enable-auto-coordination\
 --prop:/sim/rendering/multithreading-mode=AutomaticSelection\
 --allow-nasal-from-sockets\
 --vc=466\
 --heading=90\
 --altitude=3500

