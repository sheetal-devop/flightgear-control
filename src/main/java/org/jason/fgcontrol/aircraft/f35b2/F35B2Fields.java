package org.jason.fgcontrol.aircraft.f35b2;

/**
 * Property field names, descriptions, collections for the F35B2
 * 
 * @author jason
 *
 */
public abstract class F35B2Fields {

    /////////////
    // consumables
    
    public final static double FUEL_LEVEL_MIN = 0.0;
	
    public final static String FUEL_TANK_TOTAL_LEVEL_FIELD = "/consumables/fuel/total-fuel-gal_us";
    public final static String FUEL_TANK_TOTAL_LEVEL_FIELD_DESC = "Fuel total level for in gallons";
    
    public final static String FUEL_TANK_0_LEVEL_FIELD = "/consumables/fuel/tank/level-gal_us";
    public final static String FUEL_TANK_0_LEVEL_FIELD_DESC = "Fuel level for tank 0 in gallons";
    
    public final static String FUEL_TANK_0_CAPACITY_FIELD = "/consumables/fuel/tank/capacity-gal_us";
    public final static String FUEL_TANK_0_CAPACITY_FIELD_DESC = "Fuel tank capacity for tank 0 in gallons";
    
    public final static String FUEL_TANK_1_LEVEL_FIELD = "/consumables/fuel/tank[1]/level-gal_us";
    public final static String FUEL_TANK_1_LEVEL_FIELD_DESC = "Fuel level for tank 1 in gallons";
    
    public final static String FUEL_TANK_1_CAPACITY_FIELD = "/consumables/fuel/tank[1]/capacity-gal_us";
    public final static String FUEL_TANK_1_CAPACITY_FIELD_DESC = "Fuel tank capacity for tank 1 in gallons";
    
    public final static String FUEL_TANK_2_LEVEL_FIELD = "/consumables/fuel/tank[2]/level-gal_us";
    public final static String FUEL_TANK_2_LEVEL_FIELD_DESC = "Fuel level for tank 2 in gallons";
    
    public final static String FUEL_TANK_2_CAPACITY_FIELD = "/consumables/fuel/tank[2]/capacity-gal_us";
    public final static String FUEL_TANK_2_CAPACITY_FIELD_DESC = "Fuel tank capacity for tank 2 in gallons";
    
    public final static String FUEL_TANK_3_LEVEL_FIELD = "/consumables/fuel/tank[3]/level-gal_us";
    public final static String FUEL_TANK_3_LEVEL_FIELD_DESC = "Fuel level for tank 3 in gallons";
    
    public final static String FUEL_TANK_3_CAPACITY_FIELD = "/consumables/fuel/tank[3]/capacity-gal_us";
    public final static String FUEL_TANK_3_CAPACITY_FIELD_DESC = "Fuel tank capacity for tank 3 in gallons";
    
    public final static String FUEL_TANK_4_LEVEL_FIELD = "/consumables/fuel/tank[4]/level-gal_us";
    public final static String FUEL_TANK_4_LEVEL_FIELD_DESC = "Fuel level for tank 4 in gallons";
    
    public final static String FUEL_TANK_4_CAPACITY_FIELD = "/consumables/fuel/tank[4]/capacity-gal_us";
    public final static String FUEL_TANK_4_CAPACITY_FIELD_DESC = "Fuel tank capacity for tank 4 in gallons";
    
    public final static String[] CONSUMABLES_FIELDS = {
        FUEL_TANK_TOTAL_LEVEL_FIELD,
        FUEL_TANK_0_LEVEL_FIELD,
        FUEL_TANK_0_CAPACITY_FIELD,
        FUEL_TANK_1_LEVEL_FIELD,
        FUEL_TANK_1_CAPACITY_FIELD,
        FUEL_TANK_2_LEVEL_FIELD,
        FUEL_TANK_2_CAPACITY_FIELD,
        FUEL_TANK_3_LEVEL_FIELD,
        FUEL_TANK_3_CAPACITY_FIELD,
        FUEL_TANK_4_LEVEL_FIELD,
        FUEL_TANK_4_CAPACITY_FIELD
    };
    
    public final static String[] CONSUMABLES_INPUT_FIELDS = {
        FUEL_TANK_0_LEVEL_FIELD,
        FUEL_TANK_1_LEVEL_FIELD,
        FUEL_TANK_2_LEVEL_FIELD,
        FUEL_TANK_3_LEVEL_FIELD,
        FUEL_TANK_4_LEVEL_FIELD
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
     * The one engine on the f35b2 model
     * by default throttle and mixture are synced for both engines
     */
    
    public final static String ENGINE_0_CUTOFF_FIELD = "/controls/engines/engine[0]/cutoff";
    public final static String ENGINE_0_CUTOFF_FIELD_DESC = "The state of the engine 0 cutoff";
    public final static int ENGINE_CUTOFF_INT_TRUE = 1;
    public final static int ENGINE_CUTOFF_INT_FALSE = 0;
    public final static String ENGINE_CUTOFF_TRUE = String.valueOf(ENGINE_CUTOFF_INT_TRUE);
    public final static String ENGINE_CUTOFF_FALSE = String.valueOf(ENGINE_CUTOFF_INT_FALSE);
    
    public final static String ENGINE_0_MIXTURE_FIELD = "/controls/engines/engine/mixture";
    public final static String ENGINE_0_MIXTURE_FIELD_DESC = "The engine mixture percentage";
    
    public final static double MIXTURE_MAX = 1.0;
    public final static double MIXTURE_MIN = 0.0;

    public final static String ENGINE_0_THROTTLE_FIELD = "/controls/engines/engine/throttle";
    public final static String ENGINE_0_THROTTLE_FIELD_DESC = "The engine throttle percentage";
    
    public final static double THROTTLE_MAX = 1.0;
    public final static double THROTTLE_MIN = 0.0;

    public final static String AILERON_FIELD = "/controls/flight/aileron";
    public final static String AILERON_FIELD_DESC = "The aileron orientation";
    
    public final static String AILERON_TRIM_FIELD = "/controls/flight/aileron-trim";
    public final static String AILERON_TRIM_FIELD_DESC = "The aileron trim orientation";
    
    //want a marginal amount aileron trim to offset a tendency to roll clockwise
    //aileron to -0.0318 seems to be good for a left-turning flight plan maybe change depending on heading
    public final static double AILERON_DEFAULT = 0.0;
    public final static double AILERON_TRIM_DEFAULT = -0.005;
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
    
    public final static String ELEVATOR_TRIM_FIELD = "/controls/flight/elevator-trim";
    public final static String ELEVATOR_TRIM_FIELD_DESC = "The elevator trim orientation";
    
    //want a marginal amount of default lift on the elevator trim to offset gravity
    //negative elevator points the plane up, positive elevator points the plane down
    public final static double ELEVATOR_DEFAULT = -0.001;
    public final static double ELEVATOR_TRIM_DEFAULT = 0.0001;
    public final static double ELEVATOR_MAX = 1.0;
    public final static double ELEVATOR_MIN = -1.0;

    public final static String FLAPS_FIELD = "/controls/flight/flaps";
    public final static String FLAPS_FIELD_DESC = "The flaps orientation";
    public final static double FLAPS_MAX = 1.0;
    public final static double FLAPS_MIN = 0.0;
    public final static double FLAPS_DEFAULT = 0.0;

    public final static String RUDDER_FIELD = "/controls/flight/rudder";
    public final static String RUDDER_FIELD_DESC = "The rudder orientation";
    
    public final static String RUDDER_TRIM_FIELD = "/controls/flight/rudder-trim";
    public final static String RUDDER_TRIM_FIELD_DESC = "The rudder trim orientation";
    
    public final static double RUDDER_MAX = 1.0;
    public final static double RUDDER_MIN = -1.0;
    public final static double RUDDER_DEFAULT = 0.0;
    
    public final static String SPEED_BRAKE_FIELD = "/controls/flight/speedbrake";
    public final static String SPEED_BRAKE_FIELD_DESC = "The speedbrake orientation";
    
    //common values for these fields
    public final static int SPEED_BRAKE_INT_TRUE = 1;
    public final static int SPEED_BRAKE_INT_FALSE = 0;
    public final static String SPEED_BRAKE_TRUE = String.valueOf(SPEED_BRAKE_INT_TRUE);
    public final static String SPEED_BRAKE_FALSE = String.valueOf(SPEED_BRAKE_INT_FALSE);

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

    //TODO: trim fields
    public final static String[] CONTROL_FIELDS = {
        BATTERY_SWITCH_FIELD,
        ENGINE_0_CUTOFF_FIELD,
        ENGINE_0_MIXTURE_FIELD,
        ENGINE_0_THROTTLE_FIELD,
        AILERON_FIELD,
        AILERON_TRIM_FIELD,
        AUTO_COORDINATION_FIELD,
        AUTO_COORDINATION_FACTOR_FIELD,
        ELEVATOR_FIELD,
        ELEVATOR_TRIM_FIELD,
        FLAPS_FIELD,
        RUDDER_FIELD,
        RUDDER_TRIM_FIELD,
        SPEED_BRAKE_FIELD,
        PARKING_BRAKE_FIELD,
        GEAR_DOWN_FIELD
    };
    
    public final static String[] CONTROL_INPUT_FIELDS = CONTROL_FIELDS;
    
    /////////////
    // engine
    
    public final static String ENGINE_0_EXHAUST_GAS_TEMPERATURE_FIELD = "/engines/engine[0]/egt-degf";
    public final static String ENGINE_0_EXHAUST_GAS_TEMPERATURE_DESC = "The engine 0 exhaust gas temperature in fahrenheit";
   
    public final static String ENGINE_0_EXHAUST_GAS_TEMPERATURE_NORM_FIELD = "/engines/engine[0]/egt-norm";
    public final static String ENGINE_0_EXHAUST_GAS_TEMPERATURE_NORM_DESC = "The engine 0 exhaust gas temperature normalization in fahrenheit";

    public final static String ENGINE_0_FUEL_FLOW_FIELD = "/engines/engine[0]/fuel-flow-gph";
    public final static String ENGINE_0_FUEL_FLOW_DESC = "The engine 0 fuel flow in gallons per hour";
    
    public final static String ENGINE_0_OIL_PRESSURE_FIELD = "/engines/engine[0]/oil-pressure-psi";
    public final static String ENGINE_0_OIL_PRESSURE_DESC = "The engine 0 oil pressure in psi";
    
    public final static String ENGINE_0_RUNNING_FIELD = "/engines/engine[0]/running";
    public final static String ENGINE_0_RUNNING_DESC = "The engine 0 running state";
    
    //common values for these fields
    public final static int ENGINE_RUNNING_INT_TRUE = 1;
    public final static int ENGINE_RUNNING_INT_FALSE = 0;
    public final static String ENGINE_RUNNING_TRUE = String.valueOf(ENGINE_RUNNING_INT_TRUE);
    public final static String ENGINE_RUNNING_FALSE = String.valueOf(ENGINE_RUNNING_INT_FALSE);
    
    public final static String ENGINE_0_THRUST_FIELD = "/engines/engine[0]/thrust_lb";
    public final static String ENGINE_0_THRUST_DESC = "The engine 0 thrust in lbs";
    
    
    public final static String[] ENGINE_FIELDS = {
        ENGINE_0_EXHAUST_GAS_TEMPERATURE_FIELD,
        ENGINE_0_EXHAUST_GAS_TEMPERATURE_NORM_FIELD,
        ENGINE_0_FUEL_FLOW_FIELD,
        ENGINE_0_OIL_PRESSURE_FIELD,
        ENGINE_0_RUNNING_FIELD,
        ENGINE_0_THRUST_FIELD,
    };
    
    //no direct input fields. engine controls are under /controls/ in the property tree
    
    
}
