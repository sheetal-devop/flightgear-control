package org.jason.fgcontrol.flight.position;

/**
 * Lat/Lon to 5 digits precision. 
 * 
 * All the cool kids hang out in these spots.
 * 
 * @author jason
 *
 */
public abstract class KnownPositions {

    private KnownPositions() {}
    
    //Vancouver area
    public final static WaypointPosition VAN_INTER_AIRPORT_YVR = new WaypointPosition(49.19536, -123.18018, "YVR");
    public final static WaypointPosition UBC = new WaypointPosition(49.26126, -123.24684, "University of British Columbia");
    public final static WaypointPosition SPANISH_BANKS = new WaypointPosition(49.27652, -123.21306, "Spanish Banks Beach");
    public final static WaypointPosition STANLEY_PARK = new WaypointPosition(49.30486, -123.15564, "Stanley Park");
    public final static WaypointPosition LONSDALE_QUAY = new WaypointPosition(49.30974, -123.08280, "Lonsdale Quay");
    public final static WaypointPosition DEEP_COVE = new WaypointPosition(49.32675, -122.95185, "Deep Cove");
    public final static WaypointPosition MT_SEYMOUR = new WaypointPosition(49.36590, -122.94832, "Mt Seymour");
    public final static WaypointPosition GROUSE_MOUNTAIN = new WaypointPosition(49.37030, -123.09865, "Grouse Mountain Base");
    public final static WaypointPosition WEST_LION = new WaypointPosition(49.45780, -123.18659, "West Lion");
    public final static WaypointPosition LIONS_BAY = new WaypointPosition(49.45946, -123.23603, "Lions Bay");
    public final static WaypointPosition HORSESHOE_BAY = new WaypointPosition(49.37309, -123.27659, "Horseshoe Bay");
    public final static WaypointPosition LANGDALE = new WaypointPosition(49.43390, -123.47938, "Langdale");
    public final static WaypointPosition BOWEN_BAY = new WaypointPosition(49.36525, -123.42368, "Bowen Bay");
    public final static WaypointPosition MARPOLE_CC = new WaypointPosition(49.20637, -123.12349, "Marpole Curling Club");
    public final static WaypointPosition BURNABY_8RINKS = new WaypointPosition(49.25132, -122.97032, "Burnaby 8 Rinks");
    public final static WaypointPosition NEW_WESTMINSTER_QUAY = new WaypointPosition(49.20398, -122.90398, "New Westminster Quay");
    public final static WaypointPosition PLANET_ICE_DELTA = new WaypointPosition(49.14866, -122.93630, "Planet Ice - Delta");
    
    //BC Cities/towns
    public final static WaypointPosition ABBOTSFORD = new WaypointPosition(49.05061, -122.30401, "Abbotsford");
    public final static WaypointPosition LANGLEY = new WaypointPosition(49.10440, -122.66024, "Langley");
    public final static WaypointPosition CHILLIWACK = new WaypointPosition(49.15785, -121.95149, "Chilliwack");
    public final static WaypointPosition TRAIL = new WaypointPosition(49.09812, -117.70960, "Trail");
    public final static WaypointPosition GRASMERE = new WaypointPosition(49.10271, -115.08616, "Grasmere");
    public final static WaypointPosition PRINCETON = new WaypointPosition(49.45982, -120.50115, "Princeton");
    public final static WaypointPosition PENTICTON = new WaypointPosition(49.49368, -119.59009, "Penticton");
    public final static WaypointPosition KELOWNA = new WaypointPosition(49.88670, -119.48883, "Kelowna");
    public final static WaypointPosition KAMLOOPS = new WaypointPosition(50.67665, -120.32855, "Kamloops");
    public final static WaypointPosition LOUIS_CREEK = new WaypointPosition(51.14119, -120.12375, "Louis Creek");
    public final static WaypointPosition SEYMOUR_ARM = new WaypointPosition(51.23839, -118.94431, "Seymour Arm");
    public final static WaypointPosition REVELSTOKE = new WaypointPosition(50.99741, -118.18849, "Revelstoke");
    public final static WaypointPosition SHELTER_BAY = new WaypointPosition(50.63538, -117.92918, "Shelter Bay");
    public final static WaypointPosition VERNON = new WaypointPosition(50.26394, -119.27483, "Vernon");
    public final static WaypointPosition HUNDRED_MI_HOUSE = new WaypointPosition(51.64761, -121.30063, "100 Mile House");
    public final static WaypointPosition PRINCE_GEORGE = new WaypointPosition(53.91144, -122.75208, "Prince George");
    public final static WaypointPosition DAWSON_CREEK = new WaypointPosition(55.75853, -120.24130, "Dawson Creek");
    public final static WaypointPosition FORT_NELSON = new WaypointPosition(58.80258, -122.69585, "Fort Nelson");
    public final static WaypointPosition JADE_CITY = new WaypointPosition(59.25739, -129.62676, "Jade City");
    public final static WaypointPosition DEASE_LAKE = new WaypointPosition(58.43593, -129.99427, "Dease Lake");
    public final static WaypointPosition HAZELTON = new WaypointPosition(55.25575, -127.67901, "Hazelton");
    public final static WaypointPosition PRINCE_RUPERT = new WaypointPosition(54.31340, -130.31885, "Prince Rupert");
    public final static WaypointPosition BELLA_BELLA = new WaypointPosition(52.15920, -128.14856, "Bella Bella");
    public final static WaypointPosition PORT_HARDY = new WaypointPosition(50.71742, -127.49618, "Port Hardy");
    public final static WaypointPosition COMOX = new WaypointPosition(49.67373, -124.92724, "Comox");
    public final static WaypointPosition TOFINO = new WaypointPosition(49.15329, -125.90675, "Tofino");
    public final static WaypointPosition PORT_RENFREW = new WaypointPosition(48.55292, -124.42235, "Port Renfrew");
    public final static WaypointPosition SOOKE = new WaypointPosition(48.37408, -123.73550, "Sooke");
    public final static WaypointPosition VICTORIA = new WaypointPosition(48.42855, -123.36341, "Victoria");
    public final static WaypointPosition POWELL_RIVER = new WaypointPosition(49.83722, -124.52685, "Powell River");
    
    //AK cities/towns
    public final static WaypointPosition JUNEAU = new WaypointPosition(58.30099, -134.40540, "Juneau");
    

    //Cool places to snowboard
    public final static WaypointPosition BLACKCOMB = new WaypointPosition(50.09394, -122.89377, "Blackcomb");
    public final static WaypointPosition BANFF = new WaypointPosition(51.17865, -115.57060, "Banff");
    public final static WaypointPosition SNOWBIRD = new WaypointPosition(40.58309, -111.65563, "Snowbird");
    public final static WaypointPosition TAOS = new WaypointPosition(36.59579, -105.45335, "Taos");
    public final static WaypointPosition SUGARLOAF = new WaypointPosition(45.05333, -70.30800, "Sugarloaf");

    //Team alma maters
    public final static WaypointPosition RIT = new WaypointPosition(43.08458, -77.67998, "RIT");
    public final static WaypointPosition UNIV_OREGON = new WaypointPosition(44.04488, -123.07260, "University of Oregon");
    public final static WaypointPosition RUTGERS_UNIV = new WaypointPosition(40.50081, -74.44726, "Rutgers University");
    public final static WaypointPosition DREXEL_UNIV = new WaypointPosition(39.95667, -75.18970, "Drexel University");
    public final static WaypointPosition TEMPLE_UNIV = new WaypointPosition(39.98053, -75.15566, "Temple University");
    public final static WaypointPosition NORTHEASTERN_UNIV = new WaypointPosition(42.33982, -71.08919, "Northeastern University");
    
    //PTC Offices
    public final static WaypointPosition SHOREVIEW_TERR_PTC_OFFICE = new WaypointPosition(45.06446, -93.14504, "Shoreview PTC Office");
    public final static WaypointPosition OAKBROOK_TERR_PTC_OFFICE = new WaypointPosition(41.84838, -87.99037, "Oakbrook Terrace PTC Office");
    public final static WaypointPosition ALPHARETTA_PTC_OFFICE = new WaypointPosition(34.09388, -84.24291, "Alpharetta PTC Office");
    public final static WaypointPosition TUCSON_PTC_OFFICE = new WaypointPosition(32.21015, -110.90924, "Tucson PTC Office");
    public final static WaypointPosition WATERLOO_PTC_OFFICE = new WaypointPosition(43.47707, -80.54208, "Waterloo PTC Office");
    public final static WaypointPosition MONTREAL_PTC_OFFICE = new WaypointPosition(45.49306, -73.71009, "Montreal PTC Office");
    public final static WaypointPosition TROIS_RIVIERES_PTC_OFFICE = new WaypointPosition(46.34088, -72.54435, "Trois Rivieres PTC Office");
    public final static WaypointPosition ROCHESTER_PTC_OFFICE = new WaypointPosition(43.03821, -77.45739, "Rochester PTC Office");
    public final static WaypointPosition SANDIEGO_PTC_OFFICE = new WaypointPosition(32.93657, -117.23303, "San Diego PTC Office");
    public final static WaypointPosition BOSTON_PTC_OFFICE = new WaypointPosition(42.35120, -71.04463, "Boston Seaport PTC Office");
    public final static WaypointPosition PORTLAND_PTC_OFFICE = new WaypointPosition(43.65848, -70.25726, "Portland PTC Office");
    
    // Familiar Places in India
    public final static WaypointPosition AMRAVATI_CITY = new WaypointPosition(20.90422, 77.67934, "Amravati City");
    public final static WaypointPosition BHUSAVAL_CITY = new WaypointPosition(21.03964, 75.76847, "Bhusaval City");
    public final static WaypointPosition AHMEDNAGAR_CITY = new WaypointPosition(19.11046, 74.66027, "Ahmednagar City");
    public final static WaypointPosition DAUND_CITY = new WaypointPosition(18.45720, 74.56167, "Daund City");
    public final static WaypointPosition PUNE_AIRPORT = new WaypointPosition(18.57934, 73.90634, "Pune International Airport");
    public final static WaypointPosition SURAT_CITY = new WaypointPosition(21.15934, 72.73989, "Surat City");
    public final static WaypointPosition VADODARA_CITY = new WaypointPosition(22.32224, 73.09068, "Vadodara City");
    public final static WaypointPosition RATLAM_CITY = new WaypointPosition(23.33676, 75.00124, "Ratlam City");
    public final static WaypointPosition KOTA_CITY = new WaypointPosition(25.17353, 75.76456, "Kota City");
    public final static WaypointPosition DELHI_CITY = new WaypointPosition(28.64428, 76.92842, "Delhi City");
}