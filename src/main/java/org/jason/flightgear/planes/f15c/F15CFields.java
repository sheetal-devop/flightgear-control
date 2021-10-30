package org.jason.flightgear.planes.f15c;

/**
 * Property field names, descriptions, collections
 * 
 * @author jason
 *
 */
public abstract class F15CFields {

	/////////////
	// consumables

	public final static String FUEL_TANK_CAPACITY_FIELD = "/consumables/fuel/tank/capacity-gal_us";
	public final static String FUEL_TANK_CAPACITY_FIELD_DESC = "Fuel tank capacity in gallons";
	
	public final static String FUEL_TANK_LEVEL_FIELD = "/consumables/fuel/total-fuel-gal_us";
	public final static String FUEL_TANK_LEVEL_FIELD_DESC = "Fuel total level for in gallons";
	
	public final static String FUEL_TANK_0_LEVEL_FIELD = "/consumables/fuel/tank/level-gal_us";
	public final static String FUEL_TANK_0_LEVEL_FIELD_DESC = "Fuel level for tank 0 in gallons";
	
	public final static String FUEL_TANK_1_LEVEL_FIELD = "/consumables/fuel/tank[1]/level-gal_us";
	public final static String FUEL_TANK_1_LEVEL_FIELD_DESC = "Fuel level for tank 1 in gallons";
	
	public final static String FUEL_TANK_2_LEVEL_FIELD = "/consumables/fuel/tank[2]/level-gal_us";
	public final static String FUEL_TANK_2_LEVEL_FIELD_DESC = "Fuel level for tank 2 in gallons";
	
	public final static String[] CONSUMABLES_FIELDS = {
		FUEL_TANK_CAPACITY_FIELD,
		FUEL_TANK_LEVEL_FIELD,
		FUEL_TANK_0_LEVEL_FIELD,
		FUEL_TANK_1_LEVEL_FIELD,
		FUEL_TANK_2_LEVEL_FIELD
	};
	
	/////////////
	// Control
	public final static String BATTERY_SWITCH_FIELD = "/controls/electric/battery-switch";
	public final static String BATTERY_SWITCH_FIELD_DESC = "The state of the battery switch";
	
	//common values for these fields
	public final static int BATTERY_SWITCH_INT_TRUE = 1;
	public final static int BATTERY_SWITCH_INT_FALSE = 0;
	public final static String BATTERY_SWITCH_TRUE = String.valueOf(BATTERY_SWITCH_INT_TRUE);
	public final static String BATTERY_SWITCH_FALSE = String.valueOf(BATTERY_SWITCH_INT_FALSE);

	/*
	 * The McDonnell Douglas F-15 Eagle is an American twin-engine, all-weather 
	 * tactical fighter aircraft...
	 * by default throttle and mixture are synced for both engines
	 */
	public final static String MIXTURE_FIELD = "/controls/engines/engine/mixture";
	public final static String MIXTURE_FIELD_DESC = "The engine mixture percentage";
	public final static double MIXTURE_MAX = 1.0;
	public final static double MIXTURE_MIN = 0.0;

	public final static String THROTTLE_FIELD = "/controls/engines/engine/throttle";
	public final static String THROTTLE_FIELD_DESC = "The engine throttle percentage";
	public final static double THROTTLE_MAX = 1.0;
	public final static double THROTTLE_MIN = 0.0;

	public final static String AILERON_FIELD = "/controls/flight/aileron";
	public final static String AILERON_FIELD_DESC = "The aileron orientation";
	public final static double AILERON_DEFAULT = 0.0;
	public final static double AILERON_MAX = 1.0;
	public final static double AILERON_MIN = -1.0;
	
	public final static String AUTO_COORDINATION_FIELD = "/controls/flight/auto-coordination";
	public final static String AUTO_COORDINATION_FIELD_DESC = "The auto-coordination setting for controls";
	
	//common values for these fields
	public final static int AUTO_COORDINATION_INT_TRUE = 1;
	public final static int AUTO_COORDINATION_INT_FALSE = 0;
	public final static String AUTO_COORDINATION_TRUE = String.valueOf(AUTO_COORDINATION_INT_TRUE);
	public final static String AUTO_COORDINATION_FALSE = String.valueOf(AUTO_COORDINATION_INT_FALSE);


	public final static String AUTO_COORDINATION_FACTOR_FIELD = "/controls/flight/auto-coordination-factor";
	public final static String AUTO_COORDINATION_FACTOR_FIELD_DESC = "The auto-coordination factor controls";

	public final static String ELEVATOR_FIELD = "/controls/flight/elevator";
	public final static String ELEVATOR_FIELD_DESC = "The elevator orientation";
	public final static double ELEVATOR_DEFAULT = 0.0;
	public final static double ELEVATOR_MAX = 1.0;
	public final static double ELEVATOR_MIN = -1.0;

	public final static String FLAPS_FIELD = "/controls/flight/flaps";
	public final static String FLAPS_FIELD_DESC = "The flaps orientation";
	public final static double FLAPS_MAX = 1.0;
	public final static double FLAPS_MIN = 0.0;
	public final static double FLAPS_DEFAULT = 0.0;

	public final static String RUDDER_FIELD = "/controls/flight/rudder";
	public final static String RUDDER_FIELD_DESC = "The rudder orientation";
	public final static double RUDDER_MAX = 1.0;
	public final static double RUDDER_MIN = -1.0;
	public final static double RUDDER_DEFAULT = 0.0;
	
	public final static String SPEED_BRAKE_FIELD = "/controls/flight/speedbrake";
	public final static String SPEED_BRAKE_FIELD_DESC = "The speedbrake orientation";

	public final static String PARKING_BRAKE_FIELD = "/controls/gear/brake-parking";
	public final static String PARKING_BRAKE_FIELD_DESC = "The parking brake setting";
	
	//common values for these fields
	public final static int PARKING_BRAKE_INT_TRUE = 1;
	public final static int PARKING_BRAKE_INT_FALSE = 0;
	public final static String PARKING_BRAKE_TRUE = String.valueOf(PARKING_BRAKE_INT_TRUE);
	public final static String PARKING_BRAKE_FALSE = String.valueOf(PARKING_BRAKE_INT_FALSE);

	public final static String GEAR_DOWN_FIELD = "/controls/gear/gear-down";
	public final static String GEAR_DOWN_FIELD_DESC = "The gear down setting";
	
	//common values for these fields
	public final static int GEAR_DOWN_INT_TRUE = 1;
	public final static int GEAR_DOWN_INT_FALSE = 0;
	public final static String GEAR_DOWN_TRUE = String.valueOf(GEAR_DOWN_INT_TRUE);
	public final static String GEAR_DOWN_FALSE = String.valueOf(GEAR_DOWN_INT_FALSE);

	public final static String[] CONTROL_FIELDS = {
		BATTERY_SWITCH_FIELD,
		MIXTURE_FIELD,
		THROTTLE_FIELD,
		AILERON_FIELD,
		AUTO_COORDINATION_FIELD,
		AUTO_COORDINATION_FACTOR_FIELD,
		ELEVATOR_FIELD,
		FLAPS_FIELD,
		RUDDER_FIELD,
		SPEED_BRAKE_FIELD,
		PARKING_BRAKE_FIELD,
		GEAR_DOWN_FIELD
	};
	
	/////////////
	// engine
	
	public final static String ENGINE_EXHAUST_GAS_TEMPERATURE_FIELD = "/engines/engine/egt-degf";
	public final static String ENGINE_EXHAUST_GAS_TEMPERATURE_DESC = "The exhaust gas temperature in fahrenheit";
	
	public final static String ENGINE_EXHAUST_GAS_TEMPERATURE_NORM_FIELD = "/engines/engine/egt-norm";
	public final static String ENGINE_EXHAUST_GAS_TEMPERATURE_NORM_DESC = "The exhaust gas temperature normalization in fahrenheit";
	
	public final static String ENGINE_FUEL_FLOW_FIELD = "/engines/engine/fuel-flow-gph";
	public final static String ENGINE_FUEL_FLOW_DESC = "The engine fuel flow in gallons per hour";
	
	public final static String ENGINE_OIL_PRESSURE_FIELD = "/engines/engine/oil-pressure-psi";
	public final static String ENGINE_OIL_PRESSURE_DESC = "The engine oil pressure in psi";
	
	public final static String ENGINE_RUNNING_FIELD = "/engines/engine/running";
	public final static String ENGINE_RUNNING_DESC = "The engine running state";
	
	//common values for these fields
	public final static int ENGINE_RUNNING_INT_TRUE = 1;
	public final static int ENGINE_RUNNING_INT_FALSE = 0;
	public final static String ENGINE_RUNNING_TRUE = String.valueOf(ENGINE_RUNNING_INT_TRUE);
	public final static String ENGINE_RUNNING_FALSE = String.valueOf(ENGINE_RUNNING_INT_FALSE);
	
	public final static String[] ENGINE_FIELDS = {
		ENGINE_EXHAUST_GAS_TEMPERATURE_FIELD,
		ENGINE_EXHAUST_GAS_TEMPERATURE_NORM_FIELD,
		ENGINE_FUEL_FLOW_FIELD,
		ENGINE_OIL_PRESSURE_FIELD,
		ENGINE_RUNNING_FIELD
	};
	
	/////////////
	// environment
	
	public final static String DEWPOINT_FIELD = "/environment/dewpoint-degc";
	public final static String DEWPOINT_FIELD_DESC = "The dew point of the environment in celcius";
	
	public final static String EFFECTIVE_VISIBILITY_FIELD = "/environment/effective-visibility-m";
	public final static String EFFECTIVE_VISIBILITY_FIELD_DESC = "The effective visibility of the environment in meters";
	
	public final static String PRESSURE_FIELD = "/environment/pressure-inhg";
	public final static String PRESSURE_FIELD_DESC = "The atmos pressure of the environment in hg";
	
	public final static String RELATIVE_HUMIDITY_FIELD = "/environment/relative-humidity";
	public final static String RELATIVE_HUMIDITY_FIELD_DESC = "The relative humidity of the environment";
	
	public final static String TEMPERATURE_FIELD = "/environment/temperature-degf";
	public final static String TEMPERATURE_FIELD_DESC = "The temperature of the environment in fahrenheit";
	
	public final static String VISIBILITY_FIELD = "/environment/visibility-m";
	public final static String VISIBILITY_FIELD_DESC = "The visibility of the environment in meters";
	
	public final static String WIND_FROM_DOWN_FIELD = "/environment/wind-from-down-fps";
	public final static String WIND_FROM_DOWN_FIELD_DESC = "The downward windspeed of the environment in feet per second";
	
	public final static String WIND_FROM_EAST_FIELD = "/environment/wind-from-east-fps";
	public final static String WIND_FROM_EAST_FIELD_DESC = "The eastward windspeed of the environment in feet per second";
	
	public final static String WIND_FROM_NORTH_FIELD = "/environment/wind-from-north-fps";
	public final static String WIND_FROM_NORTH_FIELD_DESC = "The northward windspeed of the environment in feet/second";
	
	public final static String WINDSPEED_FIELD = "/environment/wind-speed-kt";
	public final static String WINDSPEED_FIELD_DESC = "The windspeed of the environment in knots";
	
	public final static String[] ENVIRONMENT_FIELDS = {
		DEWPOINT_FIELD,
		EFFECTIVE_VISIBILITY_FIELD,
		PRESSURE_FIELD,
		RELATIVE_HUMIDITY_FIELD,
		TEMPERATURE_FIELD,
		VISIBILITY_FIELD,
		WIND_FROM_DOWN_FIELD,
		WIND_FROM_EAST_FIELD,
		WIND_FROM_NORTH_FIELD,
		WINDSPEED_FIELD
    };
	
	/////////////
	// fdm

	public final static String FDM_DAMAGE_REPAIRING_FIELD = "/fdm/jsbsim/damage/repairing";
	public final static String FDM_DAMAGE_REPAIRING_DESC = "The FDM damage repairing state";
	
	//common values for these fields
	public final static int FDM_DAMAGE_REPAIRING_INT_TRUE = 1;
	public final static int FDM_DAMAGE_REPAIRING_INT_FALSE = 0;
	public final static String FDM_DAMAGE_REPAIRING_TRUE = String.valueOf(FDM_DAMAGE_REPAIRING_INT_TRUE);
	public final static String FDM_DAMAGE_REPAIRING_FALSE = String.valueOf(FDM_DAMAGE_REPAIRING_INT_FALSE);
	
	//fbx
	public final static String FDM_FBX_AERO_FIELD = "/fdm/jsbsim/forces/fbx-aero-lbs";
	public final static String FDM_FBX_AERO_DESC = "The FDM force vector FBX aero in pounds";
	
	public final static String FDM_FBX_EXTERNAL_FIELD = "/fdm/jsbsim/forces/fbx-external-lbs";
	public final static String FDM_FBX_EXTERNAL_DESC = "The FDM force vector FBX external in pounds";
	
	public final static String FDM_FBX_GEAR_FIELD = "/fdm/jsbsim/forces/fbx-gear-lbs";
	public final static String FDM_FBX_GEAR_DESC = "The FDM force vector FBX gear in pounds";
	
	public final static String FDM_FBX_PROP_FIELD = "/fdm/jsbsim/forces/fbx-prop-lbs";
	public final static String FDM_FBX_PROP_DESC = "The FDM force vector FBX prop in pounds";
	
	public final static String FDM_FBX_TOTAL_FIELD = "/fdm/jsbsim/forces/fbx-total-lbs";
	public final static String FDM_FBX_TOTAL_DESC = "The FDM force vector FBX total in pounds";
	
	public final static String FDM_FBX_WEIGHT_FIELD = "/fdm/jsbsim/forces/fbx-weight-lbs";
	public final static String FDM_FBX_WEIGHT_DESC = "The FDM force vector FBX weight in pounds";
	
	//fby	
	public final static String FDM_FBY_AERO_FIELD = "/fdm/jsbsim/forces/fby-aero-lbs";
	public final static String FDM_FBY_AERO_DESC = "The FDM force vector FBY aero in pounds";
	
	public final static String FDM_FBY_EXTERNAL_FIELD = "/fdm/jsbsim/forces/fby-external-lbs";
	public final static String FDM_FBY_EXTERNAL_DESC = "The FDM force vector FBY external in pounds";
	
	public final static String FDM_FBY_GEAR_FIELD = "/fdm/jsbsim/forces/fby-gear-lbs";
	public final static String FDM_FBY_GEAR_DESC = "The FDM force vector FBY gear in pounds";
	
	public final static String FDM_FBY_PROP_FIELD = "/fdm/jsbsim/forces/fby-prop-lbs";
	public final static String FDM_FBY_PROP_DESC = "The FDM force vector FBY prop in pounds";
	
	public final static String FDM_FBY_TOTAL_FIELD = "/fdm/jsbsim/forces/fby-total-lbs";
	public final static String FDM_FBY_TOTAL_DESC = "The FDM force vector FBY total in pounds";
	
	public final static String FDM_FBY_WEIGHT_FIELD = "/fdm/jsbsim/forces/fby-weight-lbs";
	public final static String FDM_FBY_WEIGHT_DESC = "The FDM force vector FBY weight in pounds";
	
	//fbz
	public final static String FDM_FBZ_AERO_FIELD = "/fdm/jsbsim/forces/fbz-aero-lbs";
	public final static String FDM_FBZ_AERO_DESC = "The FDM force vector FBZ aero in pounds";
	
	public final static String FDM_FBZ_EXTERNAL_FIELD = "/fdm/jsbsim/forces/fbz-external-lbs";
	public final static String FDM_FBZ_EXTERNAL_DESC = "The FDM force vector FBZ external in pounds";
	
	public final static String FDM_FBZ_GEAR_FIELD = "/fdm/jsbsim/forces/fbz-gear-lbs";
	public final static String FDM_FBZ_GEAR_DESC = "The FDM force vector FBZ gear in pounds";
	
	public final static String FDM_FBZ_PROP_FIELD = "/fdm/jsbsim/forces/fbz-prop-lbs";
	public final static String FDM_FBZ_PROP_DESC = "The FDM force vector FBZ prop in pounds";
	
	public final static String FDM_FBZ_TOTAL_FIELD = "/fdm/jsbsim/forces/fbz-total-lbs";
	public final static String FDM_FBZ_TOTAL_DESC = "The FDM force vector FBZ total in pounds";
	
	public final static String FDM_FBZ_WEIGHT_FIELD = "/fdm/jsbsim/forces/fbz-weight-lbs";
	public final static String FDM_FBZ_WEIGHT_DESC = "The FDM force vector FBZ weight in pounds";
	
	//fsx
	public final static String FDM_FSX_AERO_FIELD = "/fdm/jsbsim/forces/fsx-aero-lbs";
	public final static String FDM_FSX_AERO_DESC = "The FDM force vector FSX aero in pounds";
	
	//fsy
	public final static String FDM_FSY_AERO_FIELD = "/fdm/jsbsim/forces/fsy-aero-lbs";
	public final static String FDM_FSY_AERO_DESC = "The FDM force vector FSY aero in pounds";
	
	//fsz
	public final static String FDM_FSZ_AERO_FIELD = "/fdm/jsbsim/forces/fsz-aero-lbs";
	public final static String FDM_FSZ_AERO_DESC = "The FDM force vector FSZ aero in pounds";
	
	//fwy
	public final static String FDM_FWY_AERO_FIELD = "/fdm/jsbsim/forces/fwy-aero-lbs";
	public final static String FDM_FWY_AERO_DESC = "The FDM force vector FWY aero in pounds";
	
	//fwz
	public final static String FDM_FWZ_AERO_FIELD = "/fdm/jsbsim/forces/fwz-aero-lbs";
	public final static String FDM_FWZ_AERO_DESC = "The FDM force vector FWZ aero in pounds";
	
	//load factor
	public final static String FDM_LOAD_FACTOR_FIELD = "/fdm/jsbsim/forces/load-factor";
	public final static String FDM_LOAD_FACTOR_DESC = "The FDM force load factor";
	
	//lod normal
	public final static String FDM_LOD_NORM_FIELD = "/fdm/jsbsim/forces/lod-normal";
	public final static String FDM_LOD_NORM_DESC = "The FDM force lod normal";
	
	//damage
	public final static String FDM_DAMAGE_FIELD = "/fdm/jsbsim/settings/damage";
	public final static String FDM_DAMAGE_DESC = "The FDM damage setting";
	
	//common values for these fields
	public final static int FDM_DAMAGE_ENABLED_INT_TRUE = 1;
	public final static int FDM_DAMAGE_ENABLED_INT_FALSE = 0;
	public final static String FDM_DAMAGE_ENABLED_TRUE = String.valueOf(FDM_DAMAGE_ENABLED_INT_TRUE);
	public final static String FDM_DAMAGE_ENABLED_FALSE = String.valueOf(FDM_DAMAGE_ENABLED_INT_FALSE);
	
	public final static String FDM_LEFT_WING_DAMAGE_FIELD = "/fdm/jsbsim/wing-damage/left-wing";
	public final static String FDM_LEFT_WING_DAMAGE_DESC = "The FDM left wing damage setting";
	
	public final static String FDM_RIGHT_WING_DAMAGE_FIELD = "/fdm/jsbsim/wing-damage/right-wing";
	public final static String FDM_RIGHT_WING_DAMAGE_DESC = "The FDM right wing damage setting";
	
	public final static String[] FDM_FIELDS = {
		FDM_DAMAGE_REPAIRING_FIELD,
		
		FDM_FBX_AERO_FIELD,
		FDM_FBX_EXTERNAL_FIELD,
		FDM_FBX_GEAR_FIELD,
		FDM_FBX_PROP_FIELD,
		FDM_FBX_TOTAL_FIELD,
		FDM_FBX_WEIGHT_FIELD,
		
		FDM_FBY_AERO_FIELD,
		FDM_FBY_EXTERNAL_FIELD,
		FDM_FBY_GEAR_FIELD,
		FDM_FBY_PROP_FIELD,
		FDM_FBY_TOTAL_FIELD,
		FDM_FBY_WEIGHT_FIELD,
		
		FDM_FBZ_AERO_FIELD,
		FDM_FBZ_EXTERNAL_FIELD,
		FDM_FBZ_GEAR_FIELD,
		FDM_FBZ_PROP_FIELD,
		FDM_FBZ_TOTAL_FIELD,
		FDM_FBZ_WEIGHT_FIELD,
		
		FDM_FSX_AERO_FIELD,
		FDM_FSY_AERO_FIELD,
		FDM_FSZ_AERO_FIELD,
		FDM_FWZ_AERO_FIELD,
		
		FDM_LOAD_FACTOR_FIELD,
		FDM_LOD_NORM_FIELD,
		FDM_DAMAGE_FIELD,
		FDM_LEFT_WING_DAMAGE_FIELD,
		FDM_RIGHT_WING_DAMAGE_FIELD
	};
	
	/////////////
	// Orientation
	public final static String ALPHA_FIELD = "/orientation/alpha-deg";
	public final static String ALPHA_FIELD_DESC = "The alpha orientation of the plane in degrees";

	public final static String BETA_FIELD = "/orientation/beta-deg";
	public final static String BETA_FIELD_DESC = "The beta orientation of the plane in degrees";

	public final static String HEADING_FIELD = "/orientation/heading-deg";
	public final static String HEADING_FIELD_DESC = "The heading of the plane in degrees";

	public final static String HEADING_MAG_FIELD = "/orientation/heading-magnetic-deg";
	public final static String HEADING_MAG_FIELD_DESC = "The magnetc heading of the plane in degrees";

	public final static String PITCH_FIELD = "/orientation/pitch-deg";
	public final static String PITCH_FIELD_DESC = "The pitch of the plane in degrees";

	public final static String ROLL_FIELD = "/orientation/roll-deg";
	public final static String ROLL_FIELD_DESC = "The roll of the plane in degrees";

	public final static String TRACK_MAG_FIELD = "/orientation/track-magnetic-deg";
	public final static String TRACK_MAG_FIELD_DESC = "The magnetic track of the plane in degrees";

	public final static String YAW_FIELD = "/orientation/yaw-deg";
	public final static String YAW_FIELD_DESC = "The yaw of the plane in degrees";

	public final static String YAW_RATE_FIELD = "/orientation/yaw-rate-degps";
	public final static String YAW_RATE_FIELD_DESC = "The yaw rate of the plane in degrees per second";

	public final static String[] ORIENTATION_FIELDS = {
		ALPHA_FIELD,
		BETA_FIELD,
		HEADING_FIELD,
		HEADING_MAG_FIELD,
		PITCH_FIELD,
		ROLL_FIELD,
		TRACK_MAG_FIELD,
		YAW_FIELD,
		YAW_RATE_FIELD
	};
	
	/////////////
	// Position
	public final static String ALTITUDE_FIELD = "/position/altitude-ft";
	public final static String ALTITUDE_FIELD_DESC = "The altitude of the plane in feet";

	public final static String GROUND_ELEVATION_FIELD = "/position/ground-elev-ft";
	public final static String GROUND_ELEVATION_FIELD_DESC = "The ground elevation in feet";

	public final static String LATITUDE_FIELD = "/position/latitude-deg";
	public final static String LATITUDE_FIELD_DESC = "The latitude of the plane in degrees";

	public final static String LONGITUDE_FIELD = "/position/longitude-deg";
	public final static String LONGITUDE_FIELD_DESC = "The longitude of the plane in degrees";
	
	public final static String[] POSITION_FIELDS = {
		ALTITUDE_FIELD,
		GROUND_ELEVATION_FIELD,
		LATITUDE_FIELD,
		LONGITUDE_FIELD
	};

	/////////////
	// Sim
	
	//common values for these fields
//	public final static int SIM_PARKING_BRAKE_INT_TRUE = 1;
//	public final static int SIM_PARKING_BRAKE_INT_FALSE = 0;
//	public final static String SIM_PARKING_BRAKE_TRUE = String.valueOf(SIM_PARKING_BRAKE_INT_TRUE);
//	public final static String SIM_PARKING_BRAKE_FALSE = String.valueOf(SIM_PARKING_BRAKE_INT_FALSE);
	
	/////////////
	// Sim pause - just pause the sim. used a lot so it gets its own fieldset
	
	public final static String SIM_FREEZE_CLOCK_FIELD = "/sim/freeze/clock";
	public final static String SIM_FREEZE_CLOCK_FIELD_DESC = "The sim freeze clock state";
	
	public final static String SIM_FREEZE_MASTER_FIELD = "/sim/freeze/master";
	public final static String SIM_FREEZE_MASTER_FIELD_DESC = "The sim freeze master state";
	
	//common values for these fields
	public final static int SIM_FREEZE_INT_TRUE = 1;
	public final static int SIM_FREEZE_INT_FALSE = 0;
	public final static String SIM_FREEZE_TRUE = String.valueOf(SIM_FREEZE_INT_TRUE);
	public final static String SIM_FREEZE_FALSE = String.valueOf(SIM_FREEZE_INT_FALSE);
	
	public final static String[] SIM_PAUSE_FIELDS = {
		SIM_FREEZE_CLOCK_FIELD,
		SIM_FREEZE_MASTER_FIELD
	};
	
	/////////////
	// sim speed
	public final static String SIM_SPEEDUP_FIELD = "/sim/speed-up";
	public final static String SIM_SPEED_DESC = "The sim speed factor";
	
	//common values for these fields
	public final static String SIM_SPEED_DEFAULT = String.valueOf(1);
	
	public final static String[] SIM_SPEEDUP_FIELDS = {
		SIM_SPEEDUP_FIELD
	};
	
	/////////////
	// sim time
	
	public final static String SIM_TIME_ELAPSED_FIELD = "/sim/time/elapsed-sec";
	public final static String SIM_TIME_ELAPSED_DESC = "The runtime elapsed of the sim in seconds";
	
	public final static String SIM_LOCAL_DAY_SECONDS_FIELD = "/sim/time/local-day-seconds";
	public final static String SIM_LOCAL_DAY_SECONDS_DESC = "The runtime elapsed of the local day of the sim in seconds";
	
	public final static String SIM_MP_CLOCK_FIELD = "/sim/time/mp-clock-sec";
	public final static String SIM_MP_CLOCK_DESC = "The runtime elapsed of the sim in seconds, with extended precision";
	
	public final static String[] SIM_TIME_FIELDS = {
		SIM_TIME_ELAPSED_FIELD,
		SIM_LOCAL_DAY_SECONDS_FIELD,
		SIM_MP_CLOCK_FIELD
	};
	
	/////////////
	// Velocities
	public final static String AIRSPEED_FIELD = "/velocities/airspeed-kt";
	public final static String AIRSPEED_FIELD_DESC = "The current airspeed in knots";

	public final static String GROUNDSPEED_FIELD = "/velocities/groundspeed-kt";
	public final static String GROUNDSPEED_FIELD_DESC = "The current groundspeed in knots";

	public final static String VERTICALSPEED_FIELD = "/velocities/vertical-speed-fps";
	public final static String VERTICALSPEED_FIELD_DESC = "The current vertical speed in feet per second";
	
	public final static String U_BODY_FIELD = "/velocities/uBody-fps";
	public final static String U_BODY_DESC = "u-body velocity in feet per second";
	
	public final static String V_BODY_FIELD = "/velocities/vBody-fps";
	public final static String V_BODY_DESC = "v-body velocity in feet per second";
	
	public final static String W_BODY_FIELD = "/velocities/wBody-fps";
	public final static String W_BODY_DESC = "w-body velocity in feet per second";
	
	public final static String[] VELOCITIES_FIELDS = {
		AIRSPEED_FIELD,
		GROUNDSPEED_FIELD,
		VERTICALSPEED_FIELD,
		U_BODY_FIELD,
		V_BODY_FIELD,
		W_BODY_FIELD
	};
}
