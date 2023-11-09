package org.jason.fgcontrol.flight.position.app;

public class HeadingTest {
    public static void main(String []args) {
        int difference = 10;
        
        int heading = 0;
        
        System.out.println("Heading: " + heading);
        System.out.println("Raw: " + Math.sin(heading) );
        System.out.println("Raw Min: " + Math.sin(heading - difference));
        System.out.println("Raw Max: " + Math.sin(heading + difference));
        
        System.out.println("Radians: " + Math.sin( Math.toRadians(heading) ) );
        System.out.println("Radians Min: " + Math.sin( Math.toRadians( heading - difference) ));
        System.out.println("Radians Max: " + Math.sin( Math.toRadians( heading + difference) ));
        
        System.out.println("=======");
        
        heading = 90;
        System.out.println("Heading: " + heading);
        System.out.println("Raw: " + Math.sin(heading) );
        System.out.println("Raw Min: " + Math.sin(heading - difference));
        System.out.println("Raw Max: " + Math.sin(heading + difference));
        
        System.out.println("Radians: " + Math.sin( Math.toRadians(heading) ) );
        System.out.println("Radians Min: " + Math.sin( Math.toRadians( heading - difference) ));
        System.out.println("Radians Max: " + Math.sin( Math.toRadians( heading + difference) ));
        
        System.out.println("=======");
        
        heading = 180;
        System.out.println("Heading: " + heading);
        System.out.println("Raw: " + Math.sin(heading) );
        System.out.println("Raw Min: " + Math.sin(heading - difference));
        System.out.println("Raw Max: " + Math.sin(heading + difference));
        
        System.out.println("Radians: " + Math.sin( Math.toRadians(heading) ) );
        System.out.println("Radians Min: " + Math.sin( Math.toRadians( heading - difference) ));
        System.out.println("Radians Max: " + Math.sin( Math.toRadians( heading + difference) ));
        
        System.out.println("=======");
        
        heading = 270;
        System.out.println("Heading: " + heading);
        System.out.println("Raw: " + Math.sin(heading) );
        System.out.println("Raw Min: " + Math.sin(heading - difference));
        System.out.println("Raw Max: " + Math.sin(heading + difference));
        
        System.out.println("Radians: " + Math.sin( Math.toRadians(heading) ) );
        System.out.println("Radians Min: " + Math.sin( Math.toRadians( heading - difference) ));
        System.out.println("Radians Max: " + Math.sin( Math.toRadians( heading + difference) ));
        
        System.out.println("=======");
        
        heading = 360;
        System.out.println("Heading: " + heading);
        System.out.println("Raw: " + Math.sin(heading) );
        System.out.println("Raw Min: " + Math.sin(heading - difference));
        System.out.println("Raw Max: " + Math.sin(heading + difference));
        
        System.out.println("Radians: " + Math.sin( Math.toRadians(heading) ) );
        System.out.println("Radians Min: " + Math.sin( Math.toRadians( heading - difference) ));
        System.out.println("Radians Max: " + Math.sin( Math.toRadians( heading + difference) ));
    }
}
