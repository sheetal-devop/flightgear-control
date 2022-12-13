#!/bin/bash

#./c172p_*.sh HEADING START_PORT_RANGE START_LAT START_LON NAME

#this is the window geometry, not the sim video resolution, which appears fixed in windowed mode
#for most use cases use medium geometry: --geometry=640x480\ or --geometry=800x600\
#for big visuals use larger geometry: --geometry=1024x768\
#for apps user smaller geometry: --geometry=320x200\

X_RES=800
Y_RES=600

RES_GEOMETRY_STR=""$X_RES"x"$Y_RES

#pauses sim after launching

#start heading
#use heading if supplied, otherwise just head north
HEADING=${1:-0}

#known headings in degrees
#yvr -> abbotsford: 103.836
#yvr -> victoria: 189.012
#yvr -> ubc: 326.577


#max alt for 95% throttle: 5125ft
ALT=5100


################
#ports
START_PORT_RANGE=${2:-6500}

#check port range constraints (not too low, not above max)


SIM_HOST="192.168.1.244"




#port population:
#START_PORT_RANGE   => output
#+1         => telnet
#+2     => httpd 
#+3     => input 1
#+4     => input 2
#...
TELEM_OUTPUT_PORT=$START_PORT_RANGE
TELNET_PORT=$((START_PORT_RANGE+1))
CAM_VIEW_PORT=$((START_PORT_RANGE+2))
CONSUMABLES_INPUT_PORT=$((START_PORT_RANGE+3))
CONTROLS_INPUT_PORT=$((START_PORT_RANGE+4))
ENGINES_INPUT_PORT=$((START_PORT_RANGE+5))
FDM_INPUT_PORT=$((START_PORT_RANGE+6))
ORIENTATION_INPUT_PORT=$((START_PORT_RANGE+7))
POSITION_INPUT_PORT=$((START_PORT_RANGE+8))
SIM_INPUT_PORT=$((START_PORT_RANGE+9))
SIM_FREEZE_INPUT_PORT=$((START_PORT_RANGE+10))
SIM_MODEL_INPUT_PORT=$((START_PORT_RANGE+11))
SIM_SPEEDUP_INPUT_PORT=$((START_PORT_RANGE+12))
SIM_TIME_INPUT_PORT=$((START_PORT_RANGE+13))
SYSTEMS_INPUT_PORT=$((START_PORT_RANGE+14))
VELOCITIES_INPUT_PORT=$((START_PORT_RANGE+15))

########
#start position, default to yvr 49.19524, -123.18084
START_LAT=${3:-49.19524}
START_LON=${4:--123.18084}

########
#name of this aircraft
#mostly for the log directory so multiple simulators aren't logging to the same place
DATE_STR="$(date +%s)"
DEFAULT_NAME="C172P_"$DATE_STR
NAME=${5:-$DEFAULT_NAME}

BASEDIR=$(dirname "$0")

LOG_DIR=$BASEDIR/../log/fgfs_$NAME
mkdir -p $LOG_DIR

#extra rendering settings since we want to run a few instances of this
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
 --lat=$START_LAT\
 --lon=$START_LON\
 --generic=socket,out,45,$SIM_HOST,$TELEM_OUTPUT_PORT,udp,c172p_output\
 --generic=socket,in,45,$SIM_HOST,$CONSUMABLES_INPUT_PORT,udp,c172p_input_consumables\
 --generic=socket,in,45,$SIM_HOST,$CONTROLS_INPUT_PORT,udp,c172p_input_controls\
 --generic=socket,in,45,$SIM_HOST,$ENGINES_INPUT_PORT,udp,c172p_input_engines\
 --generic=socket,in,45,$SIM_HOST,$FDM_INPUT_PORT,udp,c172p_input_fdm\
 --generic=socket,in,45,$SIM_HOST,$ORIENTATION_INPUT_PORT,udp,c172p_input_orientation\
 --generic=socket,in,45,$SIM_HOST,$POSITION_INPUT_PORT,udp,c172p_input_position\
 --generic=socket,in,45,$SIM_HOST,$SIM_INPUT_PORT,udp,c172p_input_sim\
 --generic=socket,in,45,$SIM_HOST,$SIM_FREEZE_INPUT_PORT,udp,c172p_input_sim_freeze\
 --generic=socket,in,45,$SIM_HOST,$SIM_MODEL_INPUT_PORT,udp,c172p_input_sim_model\
 --generic=socket,in,45,$SIM_HOST,$SIM_SPEEDUP_INPUT_PORT,udp,c172p_input_sim_speedup\
 --generic=socket,in,45,$SIM_HOST,$SIM_TIME_INPUT_PORT,udp,c172p_input_sim_time\
 --generic=socket,in,45,$SIM_HOST,$SYSTEMS_INPUT_PORT,udp,c172p_input_systems\
 --generic=socket,in,45,$SIM_HOST,$VELOCITIES_INPUT_PORT,udp,c172p_input_velocities\
 --telnet=$TELNET_PORT\
 --httpd=$CAM_VIEW_PORT\
 --disable-ai-traffic\
 --disable-sound\
 --disable-real-weather-fetch\
 --geometry=$RES_GEOMETRY_STR\
 --texture-filtering=8\
 --disable-anti-alias-hud\
 --enable-auto-coordination\
 --prop:/environment/weather-scenario=Fair weather\
 --prop:/nasal/local_weather/enabled=false\
 --prop:/sim/rendering/bits-per-pixel=16\
 --prop:/sim/rendering/clouds3d-enable=false\
 --prop:/sim/rendering/fps-display=1\
 --prop:/sim/rendering/frame-latency-display=1\
 --prop:/sim/rendering/multithreading-mode=AutomaticSelection\
 --prop:/sim/rendering/particles=false\
 --prop:/sim/rendering/rembrant/enabled=false\
 --prop:/sim/rendering/rembrant/bloom=false\
 --prop:/sim/rendering/shadows/enabled=false\
 --vc=60\
 --heading=$HEADING\
 --altitude=$ALT\
 --log-dir=$LOG_DIR\
 --enable-freeze\
 --allow-nasal-from-sockets\
 --turbulence=0.0\
 --wind=0\@0
