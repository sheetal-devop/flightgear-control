package org.jason.fgcontrol.flight.position;

import java.util.ArrayList;

/**
 * Trips using known positions. Expected departure from YVR airport.
 * 
 * @author jason
 *
 */
public abstract class KnownRoutes {

    private KnownRoutes() {}

    public final static ArrayList<WaypointPosition> VANCOUVER_TOUR = new ArrayList<>() {

        private static final long serialVersionUID = 3487720676170767955L;
        
        {
        	add( KnownPositions.HORSESHOE_BAY );
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
            add( KnownPositions.NEW_WESTMINSTER_QUAY );
            add( KnownPositions.PLANET_ICE_DELTA );
            add( KnownPositions.VAN_INTER_AIRPORT_YVR );
        }
    };
    
    public final static ArrayList<WaypointPosition> VANCOUVER_SHORT_TOUR = new ArrayList<>() {

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
    
    public final static ArrayList<WaypointPosition> BC_TOUR = new ArrayList<>() {

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
    
    public final static ArrayList<WaypointPosition> BC_SOUTH_TOUR = new ArrayList<>() {

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
    
    public final static ArrayList<WaypointPosition> VAN_ISLAND_TOUR = new ArrayList<>() {

		private static final long serialVersionUID = -6035401483530239855L;

		{
            {
                add(KnownPositions.WEST_LION );
                add(KnownPositions.TOFINO);
                add(KnownPositions.VAN_INTER_AIRPORT_YVR);
            }
        }
    };
    
    public final static ArrayList<WaypointPosition> VAN_ISLAND_TOUR_SOUTH = new ArrayList<>() {

		private static final long serialVersionUID = 1668204581296033784L;

		{
            {
            	add(KnownPositions.LANGLEY);
            	add(KnownPositions.VICTORIA);
            	add(KnownPositions.SOOKE);
                add(KnownPositions.TOFINO);
                add(KnownPositions.VAN_INTER_AIRPORT_YVR);
            }
        }
    };
    
    public final static ArrayList<WaypointPosition> PTC_NA_OFFICE_TOUR = new ArrayList<>() {

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
}
