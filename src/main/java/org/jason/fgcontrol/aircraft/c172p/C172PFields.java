package org.jason.fgcontrol.aircraft.c172p;

/**
 * Property field names, descriptions, collections
 * 
 * @author jason
 *
 */
public abstract class C172PFields {

    private C172PFields() {};
        
    /////////////
    // consumables

    public final static String FUEL_TANK_0_CAPACITY_FIELD = "/consumables/fuel/tank/capacity-gal_us";
    public final static String FUEL_TANK_0_CAPACITY_FIELD_DESC = "Fuel tank 0 capacity in gallons";
    
    public final static String FUEL_TANK_0_LEVEL_FIELD = "/consumables/fuel/tank/level-gal_us";
    public final static String FUEL_TANK_0_LEVEL_FIELD_DESC = "Fuel tank 0 level in gallons";
    
    public final static String FUEL_TANK_0_WATER_CONTAMINATION_FIELD = "/consumables/fuel/tank/water-contamination";
    public final static String FUEL_TANK_0_WATER_CONTAMINATION_FIELD_DESC = "Water contamination amount in fuel tank 0";
    
    public final static String FUEL_TANK_1_CAPACITY_FIELD = "/consumables/fuel/tank[1]/capacity-gal_us";
    public final static String FUEL_TANK_1_CAPACITY_FIELD_DESC = "Fuel tank 1 capacity in gallons";
    
    public final static String FUEL_TANK_1_LEVEL_FIELD = "/consumables/fuel/tank[1]/level-gal_us";
    public final static String FUEL_TANK_1_LEVEL_FIELD_DESC = "Fuel tank 1 level in gallons";
    
    public final static String FUEL_TANK_1_WATER_CONTAMINATION_FIELD = "/consumables/fuel/tank[1]/water-contamination";
    public final static String FUEL_TANK_1_WATER_CONTAMINATION_FIELD_DESC = "Water contamination amount in fuel tank 1";
    

    
    public final static String[] CONSUMABLES_FIELDS = {
        FUEL_TANK_0_CAPACITY_FIELD,
        FUEL_TANK_0_LEVEL_FIELD,
        FUEL_TANK_0_WATER_CONTAMINATION_FIELD,
        FUEL_TANK_1_CAPACITY_FIELD,
        FUEL_TANK_1_LEVEL_FIELD,
        FUEL_TANK_1_WATER_CONTAMINATION_FIELD
    };
    
    public final static String[] CONSUMABLES_INPUT_FIELDS = {
        FUEL_TANK_0_LEVEL_FIELD,
        FUEL_TANK_0_WATER_CONTAMINATION_FIELD,
        FUEL_TANK_1_LEVEL_FIELD,
        FUEL_TANK_1_WATER_CONTAMINATION_FIELD
    };
    
    /////////////
    // Controls
    
    public final static String ANTI_ICE_PITOT_HEAT_FIELD = "/controls/anti-ice/pitot-heat";
    public final static String ANTI_ICE_PITOT_HEAT_DESC = "Pitot heater setting to prevent icing problems";
    
    public final static String ANTI_ICE_WINDOW_HEAT_FIELD = "/controls/anti-ice/window-heat";
    public final static String ANTI_ICE_WINDOW_HEAT_DESC = "Window heater setting to prevent icing problems";
    
    public final static String ANTI_ICE_WING_HEAT_FIELD = "/controls/anti-ice/wing-heat";
    public final static String ANTI_ICE_WING_HEAT_DESC = "Wing heater setting to prevent icing problems";

    public final static int ANTI_ICE_INT_TRUE = 1;
    public final static int ANTI_ICE_INT_FALSE = 0;
    public final static String ANTI_ICE_TRUE = String.valueOf(ANTI_ICE_INT_TRUE);
    public final static String ANTI_ICE_FALSE = String.valueOf(ANTI_ICE_INT_FALSE);
    
    
    public final static String BATTERY_SWITCH_FIELD = "/controls/electric/battery-switch";
    public final static String BATTERY_SWITCH_FIELD_DESC = "The state of the battery switch";
    
    //common values for these fields
    public final static int BATTERY_SWITCH_INT_TRUE = 1;
    public final static int BATTERY_SWITCH_INT_FALSE = 0;
    public final static String BATTERY_SWITCH_TRUE = String.valueOf(BATTERY_SWITCH_INT_TRUE);
    public final static String BATTERY_SWITCH_FALSE = String.valueOf(BATTERY_SWITCH_INT_FALSE);


    public final static String MIXTURE_FIELD = "/controls/engines/current-engine/mixture";
    public final static String MIXTURE_FIELD_DESC = "The engine mixture percentage";
    public final static double MIXTURE_MAX = 1.0;
    public final static double MIXTURE_MIN = 0.0;

    public final static String THROTTLE_FIELD = "/controls/engines/current-engine/throttle";
    public final static String THROTTLE_FIELD_DESC = "The engine throttle percentage";
    public final static double THROTTLE_MAX = 1.0;
    public final static double THROTTLE_MIN = 0.0;

    public final static String AILERON_FIELD = "/controls/flight/aileron";
    public final static String AILERON_FIELD_DESC = "The aileron orientation";
    
    public final static String AILERON_TRIM_FIELD = "/controls/flight/aileron-trim";
    public final static String AILERON_TRIM_FIELD_DESC = "The aileron trim orientation";
    public final static double aILERON_TRIM_DEFAULT = 0.022;
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
    
    public final static String ELEVATOR_TRIM_FIELD = "/controls/flight/elevator-trim";
    public final static String ELEVATOR_TRIM_FIELD_DESC = "The elevator trim orientation";
    
    public final static double ELEVATOR_DEFAULT = 0.0;
    public final static double ELEVATOR_TRIM_DEFAULT = 0.0075;
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
    public final static double RUDDER_TRIM_DEFAULT = 0.02;
    
    public final static String SPEED_BRAKE_FIELD = "/controls/flight/speedbrake";
    public final static String SPEED_BRAKE_FIELD_DESC = "The speedbrake orientation";

    public final static String PARKING_BRAKE_FIELD = "/controls/gear/brake-parking";
    public final static String PARKING_BRAKE_FIELD_DESC = "The parking brake setting";

    public final static String GEAR_DOWN_FIELD = "/controls/gear/gear-down";
    public final static String GEAR_DOWN_FIELD_DESC = "The gear down setting";
    
    //common values for these fields
    public final static int GEAR_DOWN_INT_TRUE = 1;
    public final static int GEAR_DOWN_INT_FALSE = 0;
    public final static String GEAR_DOWN_TRUE = String.valueOf(GEAR_DOWN_INT_TRUE);
    public final static String GEAR_DOWN_FALSE = String.valueOf(GEAR_DOWN_INT_FALSE);

    public final static String[] CONTROL_FIELDS = {
        ANTI_ICE_PITOT_HEAT_FIELD,
        ANTI_ICE_WINDOW_HEAT_FIELD,
        ANTI_ICE_WING_HEAT_FIELD,
        BATTERY_SWITCH_FIELD,
        MIXTURE_FIELD,
        THROTTLE_FIELD,
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
    // engines
    
    public final static String ENGINES_COMPLEX_ENGINE_PROCEDURES = "/engines/active-engine/complex-engine-procedures";
    public final static String ENGINES_COMPLEX_ENGINE_PROCEDURES_DESC = "Toggle complex engine procedures and failures for the C172P. Disable for easier flight management";
    
    //common values for these fields
    public final static int ENGINES_COMPLEX_ENGINE_PROCEDURES_INT_TRUE = 1;
    public final static int ENGINES_COMPLEX_ENGINE_PROCEDURES_INT_FALSE = 0;
    public final static String ENGINES_COMPLEX_ENGINE_PROCEDURES_TRUE = String.valueOf(ENGINES_COMPLEX_ENGINE_PROCEDURES_INT_TRUE);
    public final static String ENGINES_COMPLEX_ENGINE_PROCEDURES_FALSE = String.valueOf(ENGINES_COMPLEX_ENGINE_PROCEDURES_INT_FALSE);
    
    public final static String ENGINES_COWLING_AIR_TEMPERATURE_FIELD = "/engines/active-engine/cowling-air-temperature-degf";
    public final static String ENGINES_COWLING_AIR_TEMPERATURE_DESC = "The engine cowling air temperature in fahrenheit";
    
    public final static String ENGINES_EXHAUST_GAS_TEMPERATURE_FIELD = "/engines/active-engine/egt-degf";
    public final static String ENGINES_EXHAUST_GAS_TEMPERATURE_DESC = "The exhaust gas temperature in fahrenheit";
    
    public final static String ENGINES_EXHAUST_GAS_TEMPERATURE_NORM_FIELD = "/engines/active-engine/egt-norm";
    public final static String ENGINES_EXHAUST_GAS_TEMPERATURE_NORM_DESC = "The exhaust gas temperature normalization in fahrenheit";
    
    public final static String ENGINES_FUEL_FLOW_FIELD = "/engines/active-engine/fuel-flow-gph";
    public final static String ENGINES_FUEL_FLOW_DESC = "The engine fuel flow in gallons per hour";
    
    public final static String ENGINES_MP_OSI_FIELD = "/engines/active-engine/mp-osi";
    public final static String ENGINES_MP_OSI_DESC = "The engine mp-osi. Not sure what this is. Possibly related to engine pistons";

    public final static String ENGINES_OIL_PRESSURE_FIELD = "/engines/active-engine/oil-pressure-psi";
    public final static String ENGINES_OIL_PRESSURE_DESC = "The engine oil pressure in psi";
    
    public final static String ENGINES_OIL_TEMPERATURE_FIELD = "/engines/active-engine/oil-temperature-degf";
    public final static String ENGINES_OIL_TEMPERATURE_DESC = "The engine oil temperature in fahrenheit";

    public final static String ENGINES_RPM_FIELD = "/engines/active-engine/rpm";
    public final static String ENGINES_RPM_DESC = "The engine tachometer in rpm";
    
    public final static String ENGINES_RUNNING_FIELD = "/engines/active-engine/running";
    public final static String ENGINES_RUNNING_DESC = "The engine running state";
    
    //common values for these fields
    public final static int ENGINES_RUNNING_INT_TRUE = 1;
    public final static int ENGINES_RUNNING_INT_FALSE = 0;
    public final static String ENGINES_RUNNING_TRUE = String.valueOf(ENGINES_RUNNING_INT_TRUE);
    public final static String ENGINES_RUNNING_FALSE = String.valueOf(ENGINES_RUNNING_INT_FALSE);
    
    public final static String ENGINES_WINTER_KIT_INSTALLED = "/engines/active-engine/winter-kit-installed";
    public final static String ENGINES_WINTER_KIT_INSTALLED_DESC = "Toggle installation of the C172P winter kit. May help with icing issues.";
    
    //common values for these fields
    public final static int ENGINES_WINTER_KIT_INSTALLED_INT_TRUE = 1;
    public final static int ENGINES_WINTER_KIT_INSTALLED_INT_FALSE = 0;
    public final static String ENGINES_WINTER_KIT_INSTALLED_TRUE = String.valueOf(ENGINES_WINTER_KIT_INSTALLED_INT_TRUE);
    public final static String ENGINES_WINTER_KIT_INSTALLED_FALSE = String.valueOf(ENGINES_WINTER_KIT_INSTALLED_INT_FALSE);
    
    public final static String[] ENGINE_FIELDS = {
        ENGINES_COMPLEX_ENGINE_PROCEDURES,
        ENGINES_COWLING_AIR_TEMPERATURE_FIELD,
        ENGINES_EXHAUST_GAS_TEMPERATURE_FIELD,
        ENGINES_EXHAUST_GAS_TEMPERATURE_NORM_FIELD,
        ENGINES_FUEL_FLOW_FIELD,
        ENGINES_MP_OSI_FIELD,
        ENGINES_OIL_PRESSURE_FIELD,
        ENGINES_OIL_TEMPERATURE_FIELD,
        ENGINES_RPM_FIELD,
        ENGINES_RUNNING_FIELD,
        ENGINES_WINTER_KIT_INSTALLED
    };
    
    public final static String[] ENGINES_INPUT_FIELDS = {
        ENGINES_COMPLEX_ENGINE_PROCEDURES,
        ENGINES_WINTER_KIT_INSTALLED
    };
    
    /////////////
    //Environment
    //TODO: implement:
    // --prop:/environment/aircraft-effects/cabin-air-set=0.75\
    // --prop:/environment/aircraft-effects/cabin-heat-set=0.6\
    
    /////////////
    // Sim model
    public final static String SIM_PARKING_BRAKE_FIELD = "/sim/model/c172p/brake-parking";
    public final static String SIM_PARKING_BRAKE_FIELD_DESC = "The sim parking brake setting";
    
    //common values for these fields
    public final static int SIM_PARKING_BRAKE_INT_TRUE = 1;
    public final static int SIM_PARKING_BRAKE_INT_FALSE = 0;
    public final static String SIM_PARKING_BRAKE_TRUE = String.valueOf(SIM_PARKING_BRAKE_INT_TRUE);
    public final static String SIM_PARKING_BRAKE_FALSE = String.valueOf(SIM_PARKING_BRAKE_INT_FALSE);
    
    public final static String[] SIM_MODEL_FIELDS = {
        SIM_PARKING_BRAKE_FIELD
    };
    
    public final static String[] SIM_MODEL_INPUT_FIELDS = SIM_MODEL_FIELDS;
    
    /////////////
    // System
    
    public final static String BATTERY_CHARGE_FIELD = "/systems/electrical/battery-charge-percent";
    public final static String BATTERY_CHARGE_FIELD_DESC = "The battery charge";
    public final static double BATTERY_CHARGE_MAX = 1.0;
    public final static double BATTERY_CHARGE_MIN = 0.0;

    
    public final static String[] SYSTEM_FIELDS = {
        BATTERY_CHARGE_FIELD
    };
    
    public final static String[] SYSTEM_INPUT_FIELDS = SYSTEM_FIELDS;
}
