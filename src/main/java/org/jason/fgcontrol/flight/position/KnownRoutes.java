package org.jason.fgcontrol.flight.position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Trips using known positions.
 * 
 * @author jason
 *
 */
public abstract class KnownRoutes {

    private KnownRoutes() {}

    private final static Logger LOGGER = LoggerFactory.getLogger(KnownRoutes.class);

    public final static ArrayList<WaypointPosition> NA_CALIFORNIA = new ArrayList<WaypointPosition>() {
        // initial heading Stanford -> SFO: 328.11
        // Starting position for the route: Stanford -> SFO -> SANJOSE -> SACRAMENTO -> TAHOE -> Stanford with starting position of Standford University location 37.4276642,-122.1726403
        private static final long serialVersionUID = 6061211922312686004L;

        {
            add(KnownPositions.SFO_CITY);
            add(KnownPositions.SANJOSE_CITY);
            add(KnownPositions.SACRAMENTO_CITY);
            add(KnownPositions.TAHOE_CITY);
        }
    };

    // initial heading Nagpur to Amravati City: 261
    public final static ArrayList<WaypointPosition> PUNE_TOUR = new ArrayList<WaypointPosition>() {
             // Starting position for the route - Nagpur -> Amravati -> Bhusaval -> Ahmednagar -> Daund -> Pune Airport with starting position of Nagpur location (latitude, longitude) - 21.09016, 79.05223
            private static final long serialVersionUID = 3487720676170767955L;

            {
               add(KnownPositions.AMRAVATI_CITY);
               add(KnownPositions.BHUSAVAL_CITY);
               add(KnownPositions.AHMEDNAGAR_CITY);
               add(KnownPositions.DAUND_CITY);
               add(KnownPositions.PUNE_AIRPORT);
            }
        };

    // initial heading Mumbai Airport to Surat City: 358
    public final static ArrayList<WaypointPosition> DELHI_TOUR = new ArrayList<WaypointPosition>() {
            // Starting position for the route - Mumbai Airport -> Surat -> Vadodara -> Ratlam -> Kota -> Delhi with starting position of Mumbai Airport location (latitude, longitude) - 18.96902, 72.81795
            private static final long serialVersionUID = 3487720676170767955L;

            {
               add(KnownPositions.SURAT_CITY);
               add(KnownPositions.VADODARA_CITY);
               add(KnownPositions.RATLAM_CITY);
               add(KnownPositions.KOTA_CITY);
               add(KnownPositions.DELHI_CITY);
            }
        };

    public final static ArrayList<WaypointPosition> VANCOUVER_TOUR = new ArrayList<WaypointPosition>() {

        private static final long serialVersionUID = 3487720676170767955L;

        {
            add( KnownPositions.UBC );
            add( KnownPositions.STANLEY_PARK );
            add( KnownPositions.LONSDALE_QUAY );
            add( KnownPositions.MT_SEYMOUR );
            add( KnownPositions.GROUSE_MOUNTAIN );
            add( KnownPositions.WEST_LION );
            add( KnownPositions.LANGDALE );
            add( KnownPositions.BOWEN_BAY );
            add( KnownPositions.HORSESHOE_BAY );
            add( KnownPositions.MARPOLE_CC );
            add( KnownPositions.BURNABY_8RINKS );
            add( KnownPositions.PLANET_ICE_DELTA );
            add( KnownPositions.VAN_INTER_AIRPORT_YVR );
        }
    };

    //flight plan with minimal course corrections starting from hs bay
    public final static ArrayList<WaypointPosition> VANCOUVER_NORTH_SHORE_DEMO = new ArrayList<WaypointPosition>() {

    	private static final long serialVersionUID = -1630760784238590060L;

		{
    		add( KnownPositions.HORSESHOE_BAY );
    		add( KnownPositions.GROUSE_MOUNTAIN );
    		add( KnownPositions.DEEP_COVE );
    	}
    };

    public final static ArrayList<WaypointPosition> VANCOUVER_LOW_ALT_TOUR = new ArrayList<WaypointPosition>() {

        private static final long serialVersionUID = -1630760784238590060L;

		{
            add( KnownPositions.UBC );
            add( KnownPositions.STANLEY_PARK );
            add( KnownPositions.LONSDALE_QUAY );
            add( KnownPositions.DEEP_COVE);
            add( KnownPositions.SPANISH_BANKS);
            add( KnownPositions.HORSESHOE_BAY );
            add( KnownPositions.MARPOLE_CC );
            add( KnownPositions.BURNABY_8RINKS );
            add( KnownPositions.PLANET_ICE_DELTA );
            add( KnownPositions.VAN_INTER_AIRPORT_YVR );
        }
    };

    public final static ArrayList<WaypointPosition> VANCOUVER_SHORT_TOUR = new ArrayList<WaypointPosition>() {

        private static final long serialVersionUID = -1630760784238590060L;

		{
            add( KnownPositions.UBC );
            add( KnownPositions.LONSDALE_QUAY );
            add( KnownPositions.GROUSE_MOUNTAIN );
            add( KnownPositions.WEST_LION );
            add( KnownPositions.MARPOLE_CC );
            add( KnownPositions.BURNABY_8RINKS );
            add( KnownPositions.VAN_INTER_AIRPORT_YVR );
        }
    };

    public final static ArrayList<WaypointPosition> BC_TOUR = new ArrayList<WaypointPosition>() {

        private static final long serialVersionUID = 762842417275059036L;

        {
            {
                add(KnownPositions.ABBOTSFORD);
                add(KnownPositions.PRINCETON);
                add(KnownPositions.PENTICTON);
                add(KnownPositions.KELOWNA);
                add(KnownPositions.KAMLOOPS);
                add(KnownPositions.LOUIS_CREEK);
                add(KnownPositions.SEYMOUR_ARM);
                add(KnownPositions.SHELTER_BAY);
                add(KnownPositions.VERNON);
                add(KnownPositions.HUNDRED_MI_HOUSE);
                add(KnownPositions.PRINCE_GEORGE);
                add(KnownPositions.DAWSON_CREEK);
                add(KnownPositions.FORT_NELSON);
                add(KnownPositions.JADE_CITY);
                add(KnownPositions.DEASE_LAKE);
                add(KnownPositions.HAZELTON);
                add(KnownPositions.PRINCE_RUPERT);
                add(KnownPositions.BELLA_BELLA);
                add(KnownPositions.PORT_HARDY);
                add(KnownPositions.TOFINO);
                add(KnownPositions.PORT_RENFREW);
                add(KnownPositions.SOOKE);
                add(KnownPositions.VICTORIA);
                add(KnownPositions.VAN_INTER_AIRPORT_YVR);
            }
        }
    };

    //flight plan with minimal course corrections starting from YVR
    public final static ArrayList<WaypointPosition> BC_SOUTH_DEMO = new ArrayList<WaypointPosition>() {

		private static final long serialVersionUID = 8640158844447580989L;

		{
            {
                add(KnownPositions.TRAIL);
            }
        }
    };

    public final static ArrayList<WaypointPosition> BC_SOUTH_TOUR = new ArrayList<WaypointPosition>() {

		private static final long serialVersionUID = 9044191298704604870L;

		{
            {
                add(KnownPositions.ABBOTSFORD);
                add(KnownPositions.PRINCETON);
                add(KnownPositions.PENTICTON);
                add(KnownPositions.KELOWNA);
                add(KnownPositions.KAMLOOPS);
                add(KnownPositions.PORT_HARDY);
                add(KnownPositions.TOFINO);
                add(KnownPositions.PORT_RENFREW);
                add(KnownPositions.SOOKE);
                add(KnownPositions.VICTORIA);
                add(KnownPositions.VAN_INTER_AIRPORT_YVR);
            }
        }
    };

    public final static ArrayList<WaypointPosition> VAN_ISLAND_TOUR = new ArrayList<WaypointPosition>() {

		private static final long serialVersionUID = -6035401483530239855L;

		{
            {
                add(KnownPositions.WEST_LION );
                add(KnownPositions.TOFINO);
                add(KnownPositions.VAN_INTER_AIRPORT_YVR);
            }
        }
    };

    //initial heading yvr to chilliwack: 92
    public final static ArrayList<WaypointPosition> VAN_ISLAND_TOUR_SOUTH = new ArrayList<WaypointPosition>() {

		private static final long serialVersionUID = 1668204581296033784L;

		{
            {
            	add(KnownPositions.CHILLIWACK);
            	add(KnownPositions.VICTORIA);
                add(KnownPositions.TOFINO);
                add(KnownPositions.VAN_INTER_AIRPORT_YVR);
            }
        }
    };

    //initial heading yvr to tofino: 269
    public final static ArrayList<WaypointPosition> VAN_ISLAND_TOUR_SOUTH2 = new ArrayList<WaypointPosition>() {

		private static final long serialVersionUID = 4905334459954990812L;

		{
            {
            	add(KnownPositions.TOFINO);
            	add(KnownPositions.VICTORIA);
                add(KnownPositions.VAN_INTER_AIRPORT_YVR);
            }
        }
    };

    //initial heading yvr to powell river: 306
    public final static ArrayList<WaypointPosition> BC_WEST_COAST = new ArrayList<WaypointPosition>() {

    	private static final long serialVersionUID = 5196584768547503992L;

		{
			add(KnownPositions.POWELL_RIVER);
			add(KnownPositions.PORT_HARDY);
			add(KnownPositions.PRINCE_RUPERT);
			add(KnownPositions.JUNEAU);
			add(KnownPositions.PRINCE_RUPERT);
			add(KnownPositions.PORT_HARDY);
			add(KnownPositions.POWELL_RIVER);
			add(KnownPositions.VAN_INTER_AIRPORT_YVR);
    	}
    };

    public final static ArrayList<WaypointPosition> PTC_NA_OFFICE_TOUR = new ArrayList<WaypointPosition>() {

        private static final long serialVersionUID = 8537287930892781019L;

        {
            add(KnownPositions.SANDIEGO_PTC_OFFICE);
            add(KnownPositions.TUCSON_PTC_OFFICE);
            add(KnownPositions.ALPHARETTA_PTC_OFFICE);
            add(KnownPositions.SHOREVIEW_TERR_PTC_OFFICE);
            add(KnownPositions.OAKBROOK_TERR_PTC_OFFICE);
            add(KnownPositions.WATERLOO_PTC_OFFICE);
            add(KnownPositions.ROCHESTER_PTC_OFFICE);
            add(KnownPositions.MONTREAL_PTC_OFFICE);
            add(KnownPositions.TROIS_RIVIERES_PTC_OFFICE);
            add(KnownPositions.PORTLAND_PTC_OFFICE);
            add(KnownPositions.BOSTON_PTC_OFFICE);

        }
    };

    ///////////

    //KNOWN_ROUTES needs to execute its initializer after its prospective hashmap entry value initializers have
    //executed, or else all keys will map to null

    private final static HashMap<String, ArrayList<WaypointPosition>> KNOWN_ROUTES =
    		new HashMap<String, ArrayList<WaypointPosition>>() {

    	private static final long serialVersionUID = -8393587070456328149L;

		{
    		put("Vancouver Tour", VANCOUVER_TOUR);
    		put("Delhi Tour", DELHI_TOUR);
            put("Pune Tour", PUNE_TOUR);
    		put("Vancouver North Shore Demo", VANCOUVER_NORTH_SHORE_DEMO);
    		put("Vancouver Low Altitude Tour", VANCOUVER_LOW_ALT_TOUR);
    		put("Vancouver Short Tour", VANCOUVER_SHORT_TOUR);
    		put("BC Tour", BC_TOUR);
    		put("BC South Demo", BC_SOUTH_DEMO);
    		put("BC South Tour", BC_SOUTH_TOUR);
    		put("Vancouver Island Tour", VAN_ISLAND_TOUR);
    		put("Vancouver Island South Tour", VAN_ISLAND_TOUR_SOUTH);
    		put("Vancouver Island South 2 Tour", VAN_ISLAND_TOUR_SOUTH2);
    		put("BC West Coast Demo", BC_WEST_COAST);
    		put("PTC NA Office Tour", PTC_NA_OFFICE_TOUR);
            put("North California Tour", NA_CALIFORNIA);
    	}
    };

    //////////////////////////
    //utility methods

	/**
	 * Resolve a list of waypoints by name. Trims trailing/leading whitespace from query strings. Attempts direct
	 * string match of query string. Failing that, compares waypoint data
	 *
	 * @param query		The route name to look up
	 *
	 * @return	The resolved collection of waypoints, or null if no resolution could be made.
	 */
	public static ArrayList<WaypointPosition> lookupKnownRoute(String query) {

		query = query.trim();

		ArrayList<WaypointPosition> retval = null;

		//direct lookup
		if (KNOWN_ROUTES.containsKey(query)) {
			LOGGER.info("Direct route lookup match for query '{}'", query);
			retval = new ArrayList<WaypointPosition>();
			retval.addAll( KNOWN_ROUTES.get(query) );
		} else {
			//indirect lookup
			//loose check of known routes ignoring whitespace and case
			for( Entry<String, ArrayList<WaypointPosition>> entry : KNOWN_ROUTES.entrySet()) {
				if(entry.getKey().equalsIgnoreCase(query) ||
					entry.getKey().replaceAll("\\s", "").equalsIgnoreCase(query.replaceAll("\\s", ""))
				) {

					LOGGER.info("Indirect route lookup match for query '{}'", query);

					retval = entry.getValue();

					break;
				} else {
					LOGGER.debug("{} vs {}", entry, query );
				}
			}
		}

		if(retval == null) {
			LOGGER.warn("Route lookup failed for '{}'", query);
		}

		return retval;
	}

	/**
	 * Compare two routes by their waypoints and ordering
	 *
	 * @param route1
	 * @param route2
	 *
	 * @return	True if both routes contain the same waypoint data, false otherwise
	 */
	public static boolean isEqual( ArrayList<WaypointPosition> route1, ArrayList<WaypointPosition> route2) {

		boolean retval = true;
		if(route1 == null || route2 == null) {
			retval = false;
		} else if(route1.size() != route2.size()) {
			retval = false;
		} else {

			//compare contents. order matters
			for(int i = 0; i< route1.size(); i++) {

				LOGGER.debug("Comparing waypoint {}\n{}\nvs\n{}", i, route1.get(i), route2.get(i));

				if( !route1.get(i).equals(route2.get(i)) ) {
					retval = false;
					break;
				}
			}
		}

		return retval;
	}

    public final static ArrayList<WaypointPosition> DEFAULT_ROUTE =
    		new ArrayList<WaypointPosition>() {
    	private static final long serialVersionUID = -8393587070456328149L;

		{
    		addAll(VANCOUVER_TOUR);
    	}
    };
}