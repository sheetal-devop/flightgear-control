#!/bin/bash

#flightgear appimage setup script for flightgear-control project.

APPIMAGE_FILE=FlightGear-2020.3.17-x86_64.AppImage

#FlightGear-2020.3.17-x86_64: /home/user/flightgear-2020.3.17/FlightGear-2020.3.17-x86_64.AppImage
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
        
        #run the simulator launcher 
        FG_HOME=$FG_HOME_DIR $FG_BIN_PATH --fg-root=$FG_ROOT_DIR --launcher
    fi
fi
