#flightgear appimage setup script for flightgear-control project.

$BINARY_FILE="fgfs"

$FG_BIN_PATH=((Get-Command $BINARY_FILE).Source)

#############################
#no display config for windows

#############################

if( [string]::IsNullOrEmpty($FG_BIN_PATH) ) {
    echo "Could not find FlightGear Binary on path. Ensure the FlightGear AppImage location is on PATH: $Env:Path."
    exit 1
} else {
    echo "Found FlightGear Binary at $FG_BIN_PATH"

    $FG_BIN_DIR=(Split-Path $FG_BIN_PATH)

    if( [string]::IsNullOrEmpty($FG_BIN_DIR )) {
        echo "Could not determine parent directory for FlightGear AppImage"
        exit 1
    } else {
        echo "Found FlightGear directory at $FG_BIN_DIR"

        $FG_HOME_DIR="$Home/fgfs"
        $FG_ROOT_DIR="$Home/fgdata"

	$Env:FG_HOME = $FG_HOME_DIR

        #run the simulator launcher
        & "$FG_BIN_PATH" --fg-root=$FG_ROOT_DIR --launcher
    }
}
