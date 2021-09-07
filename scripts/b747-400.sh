#!/bin/bash

fgfs \
 --verbose\
 --prop:/nasal/local_weather/enabled=false\
 --metar=XXXX 012345Z 15003KT 12SM SCT041 FEW200 20/08 Q1015 NOSIG\
 --prop:/environment/weather-scenario=Fair weather\
 --timeofday=noon\
 --disable-rembrandt\
 --aircraft=org.flightgear.fgaddon.stable_2018.747-400\
 --state=auto\
 --fg-scenery=/usr/share/games/flightgear/Scenery\
 --fg-aircraft=/usr/share/games/flightgear/Aircraft\
 --airport=CYVR\
 --generic=socket,out,45,localhost,6501,udp,c172p_output\
 --generic=socket,in,45,,6601,udp,c172p_input_consumables\
 --generic=socket,in,45,,6602,udp,c172p_input_controls\
 --generic=socket,in,45,,6603,udp,c172p_input_orientation\
 --generic=socket,in,45,,6604,udp,c172p_input_position\
 --generic=socket,in,45,,6605,udp,c172p_input_sim\
 --generic=socket,in,45,,6606,udp,c172p_input_sim_freeze\
 --generic=socket,in,45,,6607,udp,c172p_input_velocities\
 --telnet=5501\
 --disable-sound\
 --disable-real-weather-fetch\
 --geometry=1024x768\
 --texture-filtering=4\
 --disable-anti-alias-hud\
 --enable-auto-coordination\
 --prop:/sim/rendering/multithreading-mode=AutomaticSelection\
 --allow-nasal-from-sockets

