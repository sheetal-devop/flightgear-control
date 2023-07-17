#!/bin/bash

#flightgear app setup script for flightgear-control project in Mac.
  #FlightGear-2020.3.17.dmg: Install FlightGear to Desktop or User folder Eg: /Users/<userName>/flightGear/FlightGear.app
  #FlightGear-2020.3.17-data and FlightGear-2020.3.17-update-data: Using terminal, unzip both the folders
  #Add path of fgfs (Eg: /Users/<userName>/flightGear/FlightGear.app/Contents/MacOS) to $PATH
  #FG_BIN_PATH : Set the path of fgfs folder to FG_BIN_PATH

FG_BINARY=fgfs
FG_BIN_PATH=`whereis -b $FG_BINARY | awk '{print $2}'`

#############################
#explicitly set a display

DISPLAY_STR=${DISPLAY:-":0.0"}

echo "Using display $DISPLAY_STR"

#############################

if [ -z "$FG_BIN_PATH" ]; then
    echo "Could not find FlightGear FGBinary on path."
    exit 1
else
    echo "Found FlightGear FGBinary at $FG_BIN_PATH"

    FG_BIN_DIR=`dirname $FG_BIN_PATH  | rev | cut -d/ -f4- | rev`

    if [ -z "$FG_BIN_DIR" ]; then
        echo "Could not determine parent directory for FlightGear FGBinary"
        exit 1
    else
        echo "Found FlightGear directory at $FG_BIN_DIR"

        FG_HOME_DIR=$FG_BIN_DIR/fgfs
        FG_ROOT_DIR=$FG_BIN_DIR/fgdata

        #run the simulator launcher
        DISPLAY=$DISPLAY_STR FG_HOME=$FG_HOME_DIR $FG_BIN_PATH --fg-root=$FG_ROOT_DIR --launcher
    fi
fi