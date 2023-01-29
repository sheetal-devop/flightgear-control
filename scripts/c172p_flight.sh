#!/bin/bash

#./c172p_*.sh START_PORT_RANGE HEADING START_LAT START_LON NAME

FG_AIRCRAFT=c172p

#############################
#find the AppImage on the path
#keep consistent with fg_launcher.sh

APPIMAGE_FILE=FlightGear-2020.3.17-x86_64.AppImage

FG_BIN_PATH=`whereis -b $APPIMAGE_FILE | awk '{print $2}'`

if [ -z "$FG_BIN_PATH" ]; then
    echo "Could not find FlightGear AppImage on path. Ensure the FlightGear AppImage location is in \$PATH."
    exit 1
else
    echo "Found FlightGear AppImage at $FG_BIN_PATH"
    
    FG_BIN_DIR=`dirname $FG_BIN_PATH`
    
    if [ -z "$FG_BIN_DIR" ]; then
        echo "Could not determine parent directory for FlightGear AppImage"
        exit 1
    else
        echo "Found FlightGear directory at $FG_BIN_DIR"
        
        FG_HOME_DIR=$FG_BIN_DIR/fgfs 
        FG_ROOT_DIR=$FG_BIN_DIR/fgdata 
    fi
fi

#############################

#works for external and internal input
INPUT_HOST="0.0.0.0"

TELEM_HOST="localhost"

#############################
#this is the window geometry, not the sim video resolution, which appears fixed in windowed mode
#for most use cases use medium geometry: --geometry=640x480\ or --geometry=800x600\
#for big visuals use larger geometry: --geometry=1024x768\
#for apps user smaller geometry: --geometry=320x200\

X_RES=800
Y_RES=600

RES_GEOMETRY_STR=""$X_RES"x"$Y_RES

#pauses sim after launching

################
#ports
START_PORT_RANGE=${1:-6500}

#check port range constraints (not too low, not above max)

#TODO: selectively enable httpd

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
#start heading
#use heading if supplied, otherwise just head north
HEADING=${2:-0}

#known headings in degrees
#yvr -> abbotsford: 103.836
#yvr -> victoria: 189.012
#yvr -> ubc: 326.577


#max alt for 95% throttle: 5125ft
ALT=5100

########
#start position, default to yvr 49.19524, -123.18084
START_LAT=${3:-49.19524}
START_LON=${4:--123.18084}

########
#name of this aircraft
#mostly for the log directory so multiple simulators aren't logging to the same place
DATE_STR="$(date +%s)"
DEFAULT_NAME="C172P_"$DATE_STR
NAME=${2:-$DEFAULT_NAME}

#log directory from the aircraft name
LOG_DIR=$FG_HOME_DIR/log/fgfs_$NAME

#create the directory if it doesnt already exist
if [ ! -d $LOG_DIR ]; then
    mkdir -p $LOG_DIR
    
    #check that the log directory exists now
    if [ ! -d $LOG_DIR ]; then
        echo "Error creating the simulator log directory"
        exit 1
    fi
fi

########

#switch "--enable-terrasync" with "--disable-terrasync" for offline use

#extra rendering settings since we want to run a few instances of this
FG_HOME=$FG_HOME_DIR $APPIMAGE_FILE\
 --verbose\
 --ignore-autosave\
 --enable-terrasync\
 --metar=XXXX 012345Z 15003KT 12SM SCT041 FEW200 20/08 Q1015 NOSIG\
 --timeofday=noon\
 --disable-rembrandt\
 --aircraft=c172p\
 --state=auto\
 --fog-fastest\
 --fg-root=$FG_ROOT_DIR\
 --generic=socket,out,45,$TELEM_HOST,$TELEM_OUTPUT_PORT,udp,c172p_output\
 --generic=socket,in,45,$INPUT_HOST,$CONSUMABLES_INPUT_PORT,udp,c172p_input_consumables\
 --generic=socket,in,45,$INPUT_HOST,$CONTROLS_INPUT_PORT,udp,c172p_input_controls\
 --generic=socket,in,45,$INPUT_HOST,$ENGINES_INPUT_PORT,udp,c172p_input_engines\
 --generic=socket,in,45,$INPUT_HOST,$FDM_INPUT_PORT,udp,c172p_input_fdm\
 --generic=socket,in,45,$INPUT_HOST,$ORIENTATION_INPUT_PORT,udp,c172p_input_orientation\
 --generic=socket,in,45,$INPUT_HOST,$POSITION_INPUT_PORT,udp,c172p_input_position\
 --generic=socket,in,45,$INPUT_HOST,$SIM_INPUT_PORT,udp,c172p_input_sim\
 --generic=socket,in,45,$INPUT_HOST,$SIM_FREEZE_INPUT_PORT,udp,c172p_input_sim_freeze\
 --generic=socket,in,45,$INPUT_HOST,$SIM_MODEL_INPUT_PORT,udp,c172p_input_sim_model\
 --generic=socket,in,45,$INPUT_HOST,$SIM_SPEEDUP_INPUT_PORT,udp,c172p_input_sim_speedup\
 --generic=socket,in,45,$INPUT_HOST,$SIM_TIME_INPUT_PORT,udp,c172p_input_sim_time\
 --generic=socket,in,45,$INPUT_HOST,$SYSTEMS_INPUT_PORT,udp,c172p_input_systems\
 --generic=socket,in,45,$INPUT_HOST,$VELOCITIES_INPUT_PORT,udp,c172p_input_velocities\
 --telnet=$TELNET_PORT\
 --httpd=$CAM_VIEW_PORT\
 --log-dir=$LOG_DIR\
 --disable-ai-traffic\
 --disable-sound\
 --disable-real-weather-fetch\
 --geometry=$RES_GEOMETRY_STR\
 --texture-filtering=4\
 --disable-anti-alias-hud\
 --enable-auto-coordination\
 --prop:/environment/weather-scenario=Fair weather\
 --prop:/nasal/local_weather/enabled=false\
 --prop:/environment/weather-scenario=Fair\ weather\
 --prop:/nasal/local_weather/enabled=false\
 --prop:/sim/menubar/autovisibility/enabled=true\
 --prop:/sim/menubar/visibility/enabled=false\
 --prop:/sim/rendering/fps-display=1\
 --prop:/sim/rendering/frame-latency-display=1\
 --prop:/sim/rendering/multithreading-mode=AutomaticSelection\
 --prop:/sim/rendering/redout/enabled=0\
 --prop:/sim/rendering/redout/internal/log/g-force=0\
 --prop:/sim/rendering/particles=false\
 --prop:/sim/rendering/rembrant/enabled=false\
 --prop:/sim/rendering/rembrant/bloom=false\
 --prop:/sim/rendering/shading=false\
 --prop:/sim/rendering/shadow-volume=false\
 --prop:/sim/rendering/shadows/enabled=false\
 --prop:/sim/startup/save-on-exit=false\
 --max-fps=30\
 --disable-clouds3d\
 --disable-specular-highlight\
 --vc=60\
 --heading=$HEADING\
 --altitude=$ALT\
 --lat=$START_LAT\
 --lon=$START_LON\
 --enable-freeze\
 --allow-nasal-from-sockets\
 --turbulence=0.0\
 --wind=0\@0
