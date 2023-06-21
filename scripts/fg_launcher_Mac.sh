#!/bin/bash

#flightgear appimage setup script for flightgear-control project in Mac.
  #FlightGear-2020.3.17.dmg: Install FlightGear to Desktop or User folder Eg: /Users/<userName>/flightGear/FlightGear.app
  #FlightGear-2020.3.17-data and FlightGear-2020.3.17-update-data: Using terminal, unzip both the folders
  #FG_BIN_PATH : Set the path of fgfs folder to FG_BIN_PATH
  #FG_BIN_DATA_PATH:Set the path of fgdata folder to FG_BIN_DATA_PATH
FG_BIN_PATH='/Users/jyothironda/flightGear/FlightGear.app/Contents/MacOS/fgfs'
FG_BIN_DATA_PATH='/Users/jyothironda/flightGear/fgdata'

#############################
#explicitly set a display

DISPLAY_STR=${DISPLAY:-":0.0"}

echo "Using display $DISPLAY_STR"

#############################

if [ -z "$FG_BIN_PATH" ]; then
    echo "Could not find FlightGear AppImage on path."
    exit 1
else
    echo "Found FlightGear AppImage at $FG_BIN_PATH"
    
    FG_BIN_DIR=`dirname $FG_BIN_DATA_PATH`
    
    if [ -z "$FG_BIN_DIR" ]; then
        echo "Could not determine parent directory for FlightGear AppImage"
        exit 1
    else
        echo "Found FlightGear directory at $FG_BIN_DIR"
        
        FG_HOME_DIR=$FG_BIN_DIR/fgfs
        FG_ROOT_DIR=$FG_BIN_DIR/fgdata
        
        #run the simulator launcher 
        DISPLAY=$DISPLAY_STR FG_HOME=$FG_HOME_DIR $FG_BIN_PATH --fg-root=$FG_ROOT_DIR --launcher
    fi
fi