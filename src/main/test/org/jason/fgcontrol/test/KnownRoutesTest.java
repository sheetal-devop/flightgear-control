package org.jason.fgcontrol.test;


import java.util.ArrayList;

import org.jason.fgcontrol.flight.position.KnownRoutes;
import org.jason.fgcontrol.flight.position.WaypointPosition;
import org.testng.Assert;
import org.testng.annotations.Test;

public class KnownRoutesTest {

	
	@Test
	public void testIsEqual() {
		Assert.assertTrue( KnownRoutes.isEqual( KnownRoutes.PUNE_TOUR, KnownRoutes.PUNE_TOUR) );
		Assert.assertFalse( KnownRoutes.isEqual( KnownRoutes.PUNE_TOUR, KnownRoutes.BC_SOUTH_DEMO) );

		Assert.assertFalse(KnownRoutes.isEqual( null, null) );
		
		Assert.assertTrue(KnownRoutes.isEqual( new ArrayList<WaypointPosition>(), new ArrayList<WaypointPosition>()) );
		
		Assert.assertFalse(KnownRoutes.isEqual( new ArrayList<WaypointPosition>(), KnownRoutes.PUNE_TOUR ) );
		
		Assert.assertFalse(KnownRoutes.isEqual( KnownRoutes.PUNE_TOUR, new ArrayList<WaypointPosition>() ) );
	}
	
    @Test
    public void testDirectLookup() {
        
    	Assert.assertTrue( 
    		KnownRoutes.isEqual(
    			KnownRoutes.PUNE_TOUR,
    			KnownRoutes.lookupKnownRoute("Pune Tour")
    		)
    	);
    }
    	
    @Test
    public void testIndirectLookup() {
    	//Happy case indirect 
    	Assert.assertTrue( 
    		KnownRoutes.isEqual(
    			KnownRoutes.PUNE_TOUR,
    			KnownRoutes.lookupKnownRoute("pune tour")
    		)
    	);
    }
    
    @Test
    public void testDirectLookupWhitespace() {
    	Assert.assertTrue( 
    		KnownRoutes.isEqual(
    			KnownRoutes.PUNE_TOUR,
    			KnownRoutes.lookupKnownRoute("pune tour        ")
    		)
    	);
    	
    	Assert.assertTrue( 
        	KnownRoutes.isEqual(
        		KnownRoutes.PUNE_TOUR,
        		KnownRoutes.lookupKnownRoute("        pune tour       ")
        	)
        );
    	
    	Assert.assertTrue( 
        	KnownRoutes.isEqual(
        		KnownRoutes.PUNE_TOUR,
        		KnownRoutes.lookupKnownRoute("pune tour\n")
        	)
        );
    	
    	Assert.assertTrue( 
           	KnownRoutes.isEqual(
           		KnownRoutes.PUNE_TOUR,
           		KnownRoutes.lookupKnownRoute("\tpune tour")
           	)
        );
    	
    	Assert.assertTrue( 
           	KnownRoutes.isEqual(
           		KnownRoutes.PUNE_TOUR,
           		KnownRoutes.lookupKnownRoute("pune tour\n\n\n\n")
           	)
        );
    	
    	Assert.assertTrue( 
    		KnownRoutes.isEqual(
            	KnownRoutes.PUNE_TOUR,
            	KnownRoutes.lookupKnownRoute("    pune tour\n\n\n\n")
            )
        );
    	
    	Assert.assertTrue( 
           	KnownRoutes.isEqual(
           		KnownRoutes.PUNE_TOUR,
           		KnownRoutes.lookupKnownRoute("\n\n          \n\n\npune tour\n\n\n\n")
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
