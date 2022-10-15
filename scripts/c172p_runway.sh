#!/bin/bash

#Usage: ./c172p_runway.sh START_PORT_RANGE

################
#ports
START_PORT_RANGE=${1:-6500}

#check port range constraints (not too low, not above max)



#port population:
#START_PORT_RANGE   => output
#+1         => telnet
#+2     => input 1
#+3     => input 2
#...
TELEM_OUTPUT_PORT=$START_PORT_RANGE
TELNET_PORT=$((START_PORT_RANGE+1))
CONSUMABLES_INPUT_PORT=$((START_PORT_RANGE+2))
CONTROLS_INPUT_PORT=$((START_PORT_RANGE+3))
ENGINES_INPUT_PORT=$((START_PORT_RANGE+4))
FDM_INPUT_PORT=$((START_PORT_RANGE+5))
ORIENTATION_INPUT_PORT=$((START_PORT_RANGE+6))
POSITION_INPUT_PORT=$((START_PORT_RANGE+7))
SIM_INPUT_PORT=$((START_PORT_RANGE+8))
SIM_FREEZE_INPUT_PORT=$((START_PORT_RANGE+9))
SIM_MODEL_INPUT_PORT=$((START_PORT_RANGE+10))
SIM_SPEEDUP_INPUT_PORT=$((START_PORT_RANGE+11))
SIM_TIME_INPUT_PORT=$((START_PORT_RANGE+12))
SYSTEMS_INPUT_PORT=$((START_PORT_RANGE+13))
VELOCITIES_INPUT_PORT=$((START_PORT_RANGE+14))

fgfs \
 --verbose\
 --ignore-autosave\
 --enable-terrasync\
 --metar=XXXX 012345Z 15003KT 12SM SCT041 FEW200 20/08 Q1015 NOSIG\
 --timeofday=noon\
 --disable-rembrandt\
 --aircraft-dir=/usr/share/games/flightgear/Aircraft/c172p\
 --aircraft=c172p\
 --state=auto\
 --fog-fastest\
 --fg-scenery=/usr/share/games/flightgear/Scenery\
 --fg-aircraft=/usr/share/games/flightgear/Aircraft\
 --airport=CYVR\
 --generic=socket,out,45,localhost,$TELEM_OUTPUT_PORT,udp,c172p_output\
 --generic=socket,in,45,localhost,$CONSUMABLES_INPUT_PORT,udp,c172p_input_consumables\
 --generic=socket,in,45,localhost,$CONTROLS_INPUT_PORT,udp,c172p_input_controls\
 --generic=socket,in,45,localhost,$ENGINES_INPUT_PORT,udp,c172p_input_engines\
 --generic=socket,in,45,localhost,$FDM_INPUT_PORT,udp,c172p_input_fdm\
 --generic=socket,in,45,localhost,$ORIENTATION_INPUT_PORT,udp,c172p_input_orientation\
 --generic=socket,in,45,localhost,$POSITION_INPUT_PORT,udp,c172p_input_position\
 --generic=socket,in,45,localhost,$SIM_INPUT_PORT,udp,c172p_input_sim\
 --generic=socket,in,45,localhost,$SIM_FREEZE_INPUT_PORT,udp,c172p_input_sim_freeze\
 --generic=socket,in,45,localhost,$SIM_MODEL_INPUT_PORT,udp,c172p_input_sim_model\
 --generic=socket,in,45,localhost,$SIM_SPEEDUP_INPUT_PORT,udp,c172p_input_sim_speedup\
 --generic=socket,in,45,localhost,$SIM_TIME_INPUT_PORT,udp,c172p_input_sim_time\
 --generic=socket,in,45,localhost,$SYSTEMS_INPUT_PORT,udp,c172p_input_systems\
 --generic=socket,in,45,localhost,$VELOCITIES_INPUT_PORT,udp,c172p_input_velocities\
 --telnet=$TELNET_PORT\
 --disable-ai-traffic\
 --disable-sound\
 --disable-real-weather-fetch\
 --geometry=1024x768\
 --texture-filtering=8\
 --disable-anti-alias-hud\
 --enable-auto-coordination\
 --prop:/environment/weather-scenario=Fair weather\
 --prop:/nasal/local_weather/enabled=false\
 --prop:/sim/rendering/fps-display=1\
 --prop:/sim/rendering/frame-latency-display=1\
 --prop:/sim/rendering/multithreading-mode=AutomaticSelection\
 --allow-nasal-from-sockets\
 --turbulence=0.0\
 --wind=0\@0
