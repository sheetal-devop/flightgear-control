package org.jason.fgcontrol.test;

import org.jason.fgcontrol.flight.position.KnownPositions;
import org.jason.fgcontrol.flight.position.WaypointManager;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WaypointManagerTest {

    @Test
    public void testAddAndRemove() {
        WaypointManager waypointManager = new WaypointManager();
        waypointManager.addWaypoint(KnownPositions.VAN_INTER_AIRPORT_YVR);
        waypointManager.addWaypoint(KnownPositions.ABBOTSFORD);
        waypointManager.addWaypoint(KnownPositions.PRINCETON);
        
        int startingSize = waypointManager.getWaypointCount();
        
        //starting size of the plan
        Assert.assertEquals(startingSize, 3);
        
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.VAN_INTER_AIRPORT_YVR.getName());

        Assert.assertEquals(waypointManager.getWaypointCount(), 2);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.ABBOTSFORD.getName());
        
        waypointManager.addWaypoint(KnownPositions.REVELSTOKE);
        waypointManager.addWaypoint(KnownPositions.KAMLOOPS);
        
        Assert.assertEquals(waypointManager.getWaypointCount(), 4);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.ABBOTSFORD.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.ABBOTSFORD.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), 3);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.PRINCETON.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.PRINCETON.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), 2);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.REVELSTOKE.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.REVELSTOKE.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), 1);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.KAMLOOPS.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.KAMLOOPS.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), 0);
    }
    
    
    @Test
    public void testBasicFlightPlan() {
        
        WaypointManager waypointManager = new WaypointManager();
        
        //bc tour, taking off from yvr
        waypointManager.addWaypoint(KnownPositions.VAN_INTER_AIRPORT_YVR);
        waypointManager.addWaypoint(KnownPositions.ABBOTSFORD);
        waypointManager.addWaypoint(KnownPositions.PRINCETON);
        waypointManager.addWaypoint(KnownPositions.PENTICTON);
        waypointManager.addWaypoint(KnownPositions.KELOWNA);
        waypointManager.addWaypoint(KnownPositions.KAMLOOPS);
        waypointManager.addWaypoint(KnownPositions.REVELSTOKE);
        waypointManager.addWaypoint(KnownPositions.HUNDRED_MI_HOUSE);
        waypointManager.addWaypoint(KnownPositions.PRINCE_GEORGE);
        waypointManager.addWaypoint(KnownPositions.DAWSON_CREEK);
        waypointManager.addWaypoint(KnownPositions.FORT_NELSON);
        waypointManager.addWaypoint(KnownPositions.JADE_CITY);
        waypointManager.addWaypoint(KnownPositions.DEASE_LAKE);
        waypointManager.addWaypoint(KnownPositions.HAZELTON);
        waypointManager.addWaypoint(KnownPositions.PRINCE_RUPERT);
        waypointManager.addWaypoint(KnownPositions.BELLA_BELLA);
        waypointManager.addWaypoint(KnownPositions.PORT_HARDY);
        waypointManager.addWaypoint(KnownPositions.TOFINO);
        waypointManager.addWaypoint(KnownPositions.VICTORIA);
        waypointManager.addWaypoint(KnownPositions.VAN_INTER_AIRPORT_YVR);
        
        System.out.println("Waypoints: " + waypointManager.toString());
        
        int startingSize = waypointManager.getWaypointCount();
        int expectedWaypointCount = 20;
        
        //starting size of the plan
        Assert.assertEquals(startingSize, expectedWaypointCount);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.VAN_INTER_AIRPORT_YVR.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), startingSize);
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.VAN_INTER_AIRPORT_YVR.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);

        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.ABBOTSFORD.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.ABBOTSFORD.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.PRINCETON.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.PRINCETON.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.PENTICTON.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.PENTICTON.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.KELOWNA.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.KELOWNA.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.KAMLOOPS.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.KAMLOOPS.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.REVELSTOKE.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.REVELSTOKE.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.HUNDRED_MI_HOUSE.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.HUNDRED_MI_HOUSE.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.PRINCE_GEORGE.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.PRINCE_GEORGE.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.DAWSON_CREEK.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.DAWSON_CREEK.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.FORT_NELSON.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.FORT_NELSON.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.JADE_CITY.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.JADE_CITY.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.DEASE_LAKE.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.DEASE_LAKE.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.HAZELTON.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.HAZELTON.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.PRINCE_RUPERT.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.PRINCE_RUPERT.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.BELLA_BELLA.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.BELLA_BELLA.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.PORT_HARDY.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.PORT_HARDY.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.TOFINO.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.TOFINO.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.VICTORIA.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.VICTORIA.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);
        
        Assert.assertEquals(waypointManager.getNextWaypoint().getName(), KnownPositions.VAN_INTER_AIRPORT_YVR.getName());
        Assert.assertEquals(waypointManager.getAndRemoveNextWaypoint().getName(), KnownPositions.VAN_INTER_AIRPORT_YVR.getName());
        Assert.assertEquals(waypointManager.getWaypointCount(), --expectedWaypointCount);
        
        //explicit empty check
        Assert.assertEquals(waypointManager.getWaypointCount(), 0);
    }

}
