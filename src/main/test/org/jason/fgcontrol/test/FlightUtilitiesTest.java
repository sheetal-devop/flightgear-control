package org.jason.fgcontrol.test;

import org.jason.fgcontrol.flight.util.FlightUtilities;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FlightUtilitiesTest {

    @Test
    public void testWithinHeadingThreshold() {
        //simple
        Assert.assertTrue(FlightUtilities.withinHeadingThreshold(40.0, 15.0, 46.0));
        Assert.assertTrue(FlightUtilities.withinHeadingThreshold(40.0, 15.0, 36.0));
        
        //at 90
        Assert.assertTrue(FlightUtilities.withinHeadingThreshold(90.0, 15.0, 80.0));
        Assert.assertTrue(FlightUtilities.withinHeadingThreshold(90.0, 15.0, 100.0));
        
        //threshold past zero
        //<0
        Assert.assertTrue(FlightUtilities.withinHeadingThreshold(350.0, 15.0, 0.0));
        Assert.assertTrue(FlightUtilities.withinHeadingThreshold(350.0, 20.0, 345.0));
        Assert.assertTrue(FlightUtilities.withinHeadingThreshold(350.0, 20.0, 5.0));
        Assert.assertFalse(FlightUtilities.withinHeadingThreshold(350.0, 10.0, 5.0));
        
        //>0
        Assert.assertTrue(FlightUtilities.withinHeadingThreshold(5.0, 355.0, 20.0));
        
        //at zero
        Assert.assertTrue(FlightUtilities.withinHeadingThreshold(0.0, 15.0, 0.0));
        Assert.assertTrue(FlightUtilities.withinHeadingThreshold(0.0, 15.0, 10.0));
        Assert.assertTrue(FlightUtilities.withinHeadingThreshold(0.0, 15.0, 350.0));
        
        //out of threshold
        Assert.assertFalse(FlightUtilities.withinHeadingThreshold(0.0, 15.0, 15.0));
        Assert.assertFalse(FlightUtilities.withinHeadingThreshold(0.0, 15.0, 15.0001));
        
        //small differences
        Assert.assertTrue(FlightUtilities.withinHeadingThreshold(40.0, 0.5, 40.3));
        Assert.assertTrue(FlightUtilities.withinHeadingThreshold(40.3, 0.5, 40.0));
    }
    
    @Test
    public void testHeadingComparator() {
        
        //simple cases
        //Assert.assertEquals(FlightUtilities.headingCompareTo(10.0, 15.0), FlightUtilities.HEADING_CW_ADJUST);
        //Assert.assertEquals(FlightUtilities.headingCompareTo(10.0, 10.0001), FlightUtilities.HEADING_CW_ADJUST);
        //Assert.assertEquals(FlightUtilities.headingCompareTo(10.0, 5.0), FlightUtilities.HEADING_CCW_ADJUST);
        //Assert.assertEquals(FlightUtilities.headingCompareTo(15.0, 10.0001), FlightUtilities.HEADING_CCW_ADJUST);
        //Assert.assertEquals(FlightUtilities.headingCompareTo(10.0, 10.0), FlightUtilities.HEADING_NO_ADJUST);
        
        //near 0 degs on circle
        Assert.assertEquals(FlightUtilities.headingCompareTo(350.0, 5.0), FlightUtilities.HEADING_CW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(350.0, 0.0), FlightUtilities.HEADING_CW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(0.0, 0.0001), FlightUtilities.HEADING_CW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(0.0, 350.0), FlightUtilities.HEADING_CCW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(0.0, 0.0), FlightUtilities.HEADING_NO_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(360.0, 360.0), FlightUtilities.HEADING_NO_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(360.0, 0.0), FlightUtilities.HEADING_NO_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(0.0, 360.0), FlightUtilities.HEADING_NO_ADJUST);
        
        //with +COMPARATOR_REFERENCE_DEGREES past 0 on circle
        //330 degs + 90 degs => 60 degs
        Assert.assertEquals(FlightUtilities.headingCompareTo(331.0, 5.0), FlightUtilities.HEADING_CW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(330.0, 5.0), FlightUtilities.HEADING_CW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(329.0, 5.0), FlightUtilities.HEADING_CW_ADJUST);

        Assert.assertEquals(FlightUtilities.headingCompareTo(330.0, 331.0), FlightUtilities.HEADING_CW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(330.0, 359.0), FlightUtilities.HEADING_CW_ADJUST);
        
        //with +COMPARATOR_REFERENCE_DEGREES on 0 on circle
        //270 degs + 90 degs => 0 degs
        Assert.assertEquals(FlightUtilities.headingCompareTo(270.0, 5.0), FlightUtilities.HEADING_CW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(270.0, 331.0), FlightUtilities.HEADING_CW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(270.0, 359.0), FlightUtilities.HEADING_CW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(270.0, 0.0), FlightUtilities.HEADING_CW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(270.0, 260.0), FlightUtilities.HEADING_CCW_ADJUST);
        
        //with -COMPARATOR_REFERENCE_DEGREES past 0 on circle
        //60 degs - 90 degs => 330 degs
        Assert.assertEquals(FlightUtilities.headingCompareTo(5.0, 331.0), FlightUtilities.HEADING_CCW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(5.0, 330.0), FlightUtilities.HEADING_CCW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(5.0, 329), FlightUtilities.HEADING_CCW_ADJUST);
        
        //with -COMPARATOR_REFERENCE_DEGREES on 0 on circle
        //90 degs - 90 degs => 0 degs
        Assert.assertEquals(FlightUtilities.headingCompareTo(90.0, 5.0), FlightUtilities.HEADING_CCW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(90.0, 331.0), FlightUtilities.HEADING_CCW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(90.0, 359.0), FlightUtilities.HEADING_CCW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(90.0, 0.0), FlightUtilities.HEADING_CCW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(90.0, 115.0), FlightUtilities.HEADING_CW_ADJUST);
        
        //near 90 degs on circle
        Assert.assertEquals(FlightUtilities.headingCompareTo(80.0, 95.0), FlightUtilities.HEADING_CW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(80.0, 90.0), FlightUtilities.HEADING_CW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(90.0, 90.0001), FlightUtilities.HEADING_CW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(90.0, 80.0), FlightUtilities.HEADING_CCW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(90.0, 90.0), FlightUtilities.HEADING_NO_ADJUST);
        
        //near 180 degs on circle
        Assert.assertEquals(FlightUtilities.headingCompareTo(170.0, 185.0), FlightUtilities.HEADING_CW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(170.0, 180.0), FlightUtilities.HEADING_CW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(180.0, 180.0001), FlightUtilities.HEADING_CW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(190.0, 180.0), FlightUtilities.HEADING_CCW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(180.0, 180.0), FlightUtilities.HEADING_NO_ADJUST);
        
        //near 270 degs on circle
        Assert.assertEquals(FlightUtilities.headingCompareTo(260.0, 270.0), FlightUtilities.HEADING_CW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(260.0, 270.0), FlightUtilities.HEADING_CW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(270.0, 270.0001), FlightUtilities.HEADING_CW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(280.0, 270.0), FlightUtilities.HEADING_CCW_ADJUST);
        Assert.assertEquals(FlightUtilities.headingCompareTo(270.0, 270.0), FlightUtilities.HEADING_NO_ADJUST);
    }
    
    @Test
    public void testDetermineCourseChangeAdjustment() {

    	double currentHeading;
    	
    	//no adjustment, expect the current heading
    	currentHeading = 90.0;
    	Assert.assertEquals(currentHeading, FlightUtilities.determineCourseChangeAdjustment(currentHeading, 3.0, 92.0));
    	Assert.assertEquals(currentHeading, FlightUtilities.determineCourseChangeAdjustment(currentHeading, 3.0, 88.0));
    	
    	//adjust clockwise by adjustment amount
    	currentHeading = 100.0;
    	Assert.assertEquals(103.0, FlightUtilities.determineCourseChangeAdjustment(currentHeading, 3.0, 110.0));
    	
    	//adjust clockwise by adjustment amount over zero cw
    	currentHeading = 359.0;
    	Assert.assertEquals(1.0, FlightUtilities.determineCourseChangeAdjustment(currentHeading, 2.0, 1.5));
    	
    	//no adjustment, within threshold over zero cw
    	currentHeading = 359.0;
    	Assert.assertEquals(359.0, FlightUtilities.determineCourseChangeAdjustment(currentHeading, 20.0, 10.0));
    	
    	//adjust clockwise by adjustment amount over zero cw
    	currentHeading = 359.0;
    	Assert.assertEquals(1.0, FlightUtilities.determineCourseChangeAdjustment(currentHeading, 2.0, 1.5));
    	
    	//no adjustment, within threshold over zero ccw
    	currentHeading = 2.0;
    	Assert.assertEquals(2.0, FlightUtilities.determineCourseChangeAdjustment(currentHeading, 20.0, 350.0));
    	
    	//ccw adjustment, within threshold over zero ccw
    	currentHeading = 2.0;
    	Assert.assertEquals(357.0, FlightUtilities.determineCourseChangeAdjustment(currentHeading, 5.0, 350.0));
    	
    	//90 degs
    	currentHeading = 60.0;
    	Assert.assertEquals(150.0, FlightUtilities.determineCourseChangeAdjustment(currentHeading, 90.0, 240.0));
    	
    	currentHeading = 340.0;
    	Assert.assertEquals(70.0, FlightUtilities.determineCourseChangeAdjustment(currentHeading, 90.0, 100.0));
    	
    	//randoms
    	currentHeading = 20.0;
    	Assert.assertEquals(25.0, FlightUtilities.determineCourseChangeAdjustment(currentHeading, 5.0, 150.0));
    	
    	currentHeading = 120.0;
    	Assert.assertEquals(115.0, FlightUtilities.determineCourseChangeAdjustment(currentHeading, 5.0, 20.0));
    }
    
    @Test
    public void testWithinPitchThreshold() {
    	//pitch is -180 to 180
    	
    	Assert.assertTrue(FlightUtilities.withinPitchThreshold(0.0, 5.0, 0.0));
    	Assert.assertTrue(FlightUtilities.withinPitchThreshold(0.5, 1.0, 0.5));

    	
    	Assert.assertTrue(FlightUtilities.withinPitchThreshold(-2.0, 5.0, 0.0));    	
    	
    	Assert.assertFalse(FlightUtilities.withinPitchThreshold(0.0, 1.0, 2.0));
    	Assert.assertFalse(FlightUtilities.withinPitchThreshold(0.5, 1.5, 2.5));
    	
    	Assert.assertFalse(FlightUtilities.withinPitchThreshold(-2.0, 1.0, 0.0));
    	Assert.assertFalse(FlightUtilities.withinPitchThreshold(-2.2, 1.0, 0.2));
    	    	
    	//boundaries
    	Assert.assertFalse(FlightUtilities.withinPitchThreshold(5.0, 0.0, 5.0));
    	Assert.assertFalse(FlightUtilities.withinPitchThreshold(-5.0, 0.0, -5.0));
    	
    	Assert.assertFalse(FlightUtilities.withinPitchThreshold(5.0, 0.0, 4.9));
    	Assert.assertFalse(FlightUtilities.withinPitchThreshold(-5.0, 0.0, -4.9));
    }
    
    @Test
    public void testWithinRollThreshold() {
    	//roll is +180 to -180
    	
    	Assert.assertTrue(FlightUtilities.withinRollThreshold(0.0, 5.0, 0.0));
    	Assert.assertTrue(FlightUtilities.withinRollThreshold(0.5, 1.0, 0.5));

    	
    	Assert.assertTrue(FlightUtilities.withinRollThreshold(-2.0, 5.0, 0.0));    	
    	
    	Assert.assertFalse(FlightUtilities.withinRollThreshold(0.0, 1.0, 2.0));
    	Assert.assertFalse(FlightUtilities.withinRollThreshold(0.5, 1.5, 2.5));
    	
    	Assert.assertFalse(FlightUtilities.withinRollThreshold(-2.0, 1.0, 0.0));
    	Assert.assertFalse(FlightUtilities.withinRollThreshold(-2.2, 1.0, 0.2));
    	    	
    	//boundaries
    	Assert.assertFalse(FlightUtilities.withinRollThreshold(5.0, 0.0, 5.0));
    	Assert.assertFalse(FlightUtilities.withinRollThreshold(-5.0, 0.0, -5.0));
    	
    	Assert.assertFalse(FlightUtilities.withinRollThreshold(5.0, 0.0, 4.9));
    	Assert.assertFalse(FlightUtilities.withinRollThreshold(-5.0, 0.0, -4.9));
    }
}
