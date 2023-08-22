package org.jason.fgcontrol.test;


import java.util.ArrayList;

import org.jason.fgcontrol.flight.position.KnownRoutes;
import org.jason.fgcontrol.flight.position.WaypointPosition;
import org.testng.Assert;
import org.testng.annotations.Test;

public class KnownRoutesTest {

	
	@Test
	public void testIsEqual() {
		Assert.assertTrue( KnownRoutes.isEqual( KnownRoutes.VANCOUVER_TOUR, KnownRoutes.VANCOUVER_TOUR) );
		Assert.assertFalse( KnownRoutes.isEqual( KnownRoutes.VANCOUVER_TOUR, KnownRoutes.BC_SOUTH_DEMO) );

		Assert.assertFalse(KnownRoutes.isEqual( null, null) );
		
		Assert.assertTrue(KnownRoutes.isEqual( new ArrayList<WaypointPosition>(), new ArrayList<WaypointPosition>()) );
		
		Assert.assertFalse(KnownRoutes.isEqual( new ArrayList<WaypointPosition>(), KnownRoutes.VANCOUVER_TOUR ) );
		
		Assert.assertFalse(KnownRoutes.isEqual( KnownRoutes.VANCOUVER_TOUR, new ArrayList<WaypointPosition>() ) );
	}
	
    @Test
    public void testDirectLookup() {
        
    	Assert.assertTrue( 
    		KnownRoutes.isEqual(
    			KnownRoutes.VANCOUVER_TOUR,
    			KnownRoutes.lookupKnownRoute("Vancouver Tour")
    		)
    	);
    }
    	
    @Test
    public void testIndirectLookup() {
    	//Happy case indirect 
    	Assert.assertTrue( 
    		KnownRoutes.isEqual(
    			KnownRoutes.VANCOUVER_TOUR,
    			KnownRoutes.lookupKnownRoute("vancouver tour")
    		)
    	);
    }
    
    @Test
    public void testDirectLookupWhitespace() {
    	Assert.assertTrue( 
    		KnownRoutes.isEqual(
    			KnownRoutes.VANCOUVER_TOUR,
    			KnownRoutes.lookupKnownRoute("vancouver tour        ")
    		)
    	);
    	
    	Assert.assertTrue( 
        	KnownRoutes.isEqual(
        		KnownRoutes.VANCOUVER_TOUR,
        		KnownRoutes.lookupKnownRoute("        vancouver tour       ")
        	)
        );
    	
    	Assert.assertTrue( 
        	KnownRoutes.isEqual(
        		KnownRoutes.VANCOUVER_TOUR,
        		KnownRoutes.lookupKnownRoute("vancouver tour\n")
        	)
        );
    	
    	Assert.assertTrue( 
           	KnownRoutes.isEqual(
           		KnownRoutes.VANCOUVER_TOUR,
           		KnownRoutes.lookupKnownRoute("\tvancouver tour")
           	)
        );
    	
    	Assert.assertTrue( 
           	KnownRoutes.isEqual(
           		KnownRoutes.VANCOUVER_TOUR,
           		KnownRoutes.lookupKnownRoute("vancouver tour\n\n\n\n")
           	)
        );
    	
    	Assert.assertTrue( 
    		KnownRoutes.isEqual(
            	KnownRoutes.VANCOUVER_TOUR,
            	KnownRoutes.lookupKnownRoute("    vancouver tour\n\n\n\n")
            )
        );
    	
    	Assert.assertTrue( 
           	KnownRoutes.isEqual(
           		KnownRoutes.VANCOUVER_TOUR,
           		KnownRoutes.lookupKnownRoute("\n\n          \n\n\nvancouver tour\n\n\n\n")
           	)
        );
    }
    
    @Test
    public void testInDirectLookupWhitespace() {
    	Assert.assertTrue( 
    		KnownRoutes.isEqual(
            	KnownRoutes.VAN_ISLAND_TOUR_SOUTH, 
            	KnownRoutes.lookupKnownRoute("Vancouver     Island South Tour")
            )
        );
    	
    	Assert.assertTrue( 
        	KnownRoutes.isEqual(
               	KnownRoutes.VAN_ISLAND_TOUR_SOUTH, 
               	KnownRoutes.lookupKnownRoute("Vancouver     Island South Tour\n")
            )
        );
    }
    
    @Test
    public void testLookupFailure() {
    	//no match
    	Assert.assertNull(
    		KnownRoutes.lookupKnownRoute("Highway to hell")
        );
    }
}
