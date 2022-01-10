#!/bin/bash

#TODO: generate some of this from a template, since the ports are defined in the source

#for visuals use larger geometry: --geometry=1024x768\
#for apps user smaller geometry: --geometry=320x200\

#pauses sim after launching

#use heading if supplied, otherwise just head north
HEADING=${1:-0}

fgfs \
 --verbose\
 --ignore-autosave\
 --enable-terrasync\
 --metar=XXXX 012345Z 15003KT 12SM SCT041 FEW200 20/08 Q1015 NOSIG\
 --timeofday=noon\
 --disable-rembrandt\
 --aircraft=org.flightgear.fgaddon.stable_2018.f15c\
 --fog-fastest\
 --fg-scenery=/usr/share/games/flightgear/Scenery\
 --fg-aircraft=/usr/share/games/flightgear/Aircraft\
 --airport=CYVR\
 --generic=socket,out,45,localhost,6501,udp,f15c_output\
 --generic=socket,in,45,localhost,6601,udp,f15c_input_consumables\
 --generic=socket,in,45,localhost,6602,udp,f15c_input_controls\
 --generic=socket,in,45,localhost,6603,udp,f15c_input_engines\
 --generic=socket,in,45,localhost,6604,udp,f15c_input_fdm\
 --generic=socket,in,45,localhost,6605,udp,f15c_input_orientation\
 --generic=socket,in,45,localhost,6606,udp,f15c_input_position\
 --generic=socket,in,45,localhost,6607,udp,f15c_input_sim\
 --generic=socket,in,45,localhost,6608,udp,f15c_input_sim_freeze\
 --generic=socket,in,45,localhost,6609,udp,f15c_input_sim_model\
 --generic=socket,in,45,localhost,6610,udp,f15c_input_sim_speedup\
 --generic=socket,in,45,localhost,6611,udp,f15c_input_sim_time\
 --generic=socket,in,45,localhost,6612,udp,f15c_input_system\
 --generic=socket,in,45,localhost,6613,udp,f15c_input_velocities\
 --telnet=5501\
 --disable-ai-traffic\
 --disable-sound\
 --disable-real-weather-fetch\
 --geometry=1024x768\
 --texture-filtering=8\
 --disable-anti-alias-hud\
 --enable-auto-coordination\
 --prop:/environment/weather-scenario=Fair\ weather\
 --prop:/nasal/local_weather/enabled=false\
 --prop:/sim/rendering/fps-display=1\
 --prop:/sim/rendering/frame-latency-display=1\
 --prop:/sim/rendering/multithreading-mode=AutomaticSelection\
 --prop:/sim/rendering/redout/enabled=0\
 --prop:/sim/rendering/redout/internal/log/g-force=0\
 --vc=600\
 --heading=$HEADING\
 --altitude=9000\
 --enable-freeze\
 --allow-nasal-from-sockets\
 --turbulence=0.0\
 --wind=0\@0

