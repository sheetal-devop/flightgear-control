package org.jason.fgcontrol.connection.sockets.app;

/**
 * A test of selectively updating fields in a custom-defined input protocol.
 *
 */
public class ControlInputWriteUpdate {
	
    public static void main(String[] args) {
        
        if(args.length != 2) {
        	System.err.println("Usage: ControlInputWriteUpdate [host] [inputPort]");
        	System.exit(-1);
        }
        
        int inputPort = -1;
        String host = args[0];
    	
        try {
        	inputPort = Integer.parseInt(args[1]);
        } catch (Exception e) {
        	
        } finally {
        	if(inputPort == -1 ) {
        		System.err.println("Invalid port");
        		System.exit(-1);
        	}
        }

        //concept- 
        //for the protocol below, set values for c172p control surface fields and write the input to the simulator
        //sleep noticeably
        //then send an update where only 1 of the fields is defined and the rest are empty
        //leave simulator open

		// test input//////////////

        //starting state
        String fgInput = "\n" +
		"0.0," +	//pitot-heat
        "0.0," +	//window-heat
		"0.0," +	//wing-heat
        "0.0," +	//battery-switch
        "0.2," +	//mixture
        "0.2," +	//throttle
        "0.75," +	//aileron
        "0.25," +	//aileron trim
        "0.0," +	//auto-coordination
        "0.0," +	//auto-coordination factor
        "0.75," +	//elevator
        "0.25," +	//elevator trim
        "0.0," +	//flaps
        "0.75," +	//rudder
        "0.25," +	//rudder trim
		"1.0," +	//speedbrake
        "1.0," +	//brake-parking
		"0.0" +	//gear-down
		"\n";
        
        //change the elevator to -0.5
        //expect /controls/flight/elevator to be set to -0.5, other fields retain values
        //result success
        String fgInputUpdateFull = "\n" +
		"0.0," +	//pitot-heat
        "0.0," +	//window-heat
		"0.0," +	//wing-heat
        "0.0," +	//battery-switch
        "0.2," +	//mixture
        "0.2," +	//throttle
        "0.75," +	//aileron
        "0.25," +	//aileron trim
        "0.0," +	//auto-coordination
        "0.0," +	//auto-coordination factor
        "-0.5," +	//elevator
        "0.25," +	//elevator trim
        "0.0," +	//flaps
        "0.75," +	//rudder
        "0.25," +	//rudder trim
		"1.0," +	//speedbrake
        "1.0," +	//brake-parking
		"0.0" +	//gear-down
		"\n";
        
        //change just the aileron to -0.25
        //expect /controls/flight/aileron to be set to -0.25, other fields retain values
        //result fail- other fields are zeroed out
        String fgInputUpdateWithEmptyFields = "\n" +
		"," +	//pitot-heat
        "," +	//window-heat
		"," +	//wing-heat
        "," +	//battery-switch
        "," +	//mixture
        "," +	//throttle
        "-0.25," +	//aileron
        "," +	//aileron trim
        "," +	//auto-coordination
        "," +	//auto-coordination factor
        "," +	//elevator
        "," +	//elevator trim
        "," +	//flaps
        "," +	//rudder
        "," +	//rudder trim
		"," +	//speedbrake
        "," +	//brake-parking
		"" +	//gear-down
		"\n";
        
        //change just the aileron to -0.25
        //expect /controls/flight/aileron to be set to -0.25, other fields retain values
        //result fail- other fields are zeroed out
        String fgInputUpdateWithSpacedFields = "\n" +
		" ," +	//pitot-heat
        " ," +	//window-heat
		" ," +	//wing-heat
        " ," +	//battery-switch
        " ," +	//mixture
        " ," +	//throttle
        "-0.25," +	//aileron
        " ," +	//aileron trim
        " ," +	//auto-coordination
        " ," +	//auto-coordination factor
        " ," +	//elevator
        " ," +	//elevator trim
        " ," +	//flaps
        " ," +	//rudder
        " ," +	//rudder trim
		" ," +	//speedbrake
        " ," +	//brake-parking
		"" +	//gear-down
		"\n";
        
        //change just the aileron to -0.25
        //expect /controls/flight/aileron to be set to -0.25, other fields retain values
        //result fail- crashes simulator :(
        String fgInputUpdateWithNaNFields = "\n" +
		"NaN," +	//pitot-heat
        "NaN," +	//window-heat
		"NaN," +	//wing-heat
        "NaN," +	//battery-switch
        "NaN," +	//mixture
        "NaN," +	//throttle
        "-0.25," +	//aileron
        "NaN," +	//aileron trim
        "NaN," +	//auto-coordination
        "NaN," +	//auto-coordination factor
        "NaN," +	//elevator
        "NaN," +	//elevator trim
        "NaN," +	//flaps
        "NaN," +	//rudder
        "NaN," +	//rudder trim
		"NaN," +	//speedbrake
        "NaN," +	//brake-parking
		"" +	//gear-down
		"\n";
        
        //change just the aileron to -0.25
        //expect /controls/flight/aileron to be set to -0.25, other fields retain values
        //result fail- sets aileron value and other fields are zeroed out 
        String fgInputUpdateWithJunkFields = "\n" +
		"derp," +	//pitot-heat
        "derp," +	//window-heat
		"derp," +	//wing-heat
        "derp," +	//battery-switch
        "derp," +	//mixture
        "derp," +	//throttle
        "-0.25," +	//aileron
        "derp," +	//aileron trim
        "derp," +	//auto-coordination
        "derp," +	//auto-coordination factor
        "derp," +	//elevator
        "derp," +	//elevator trim
        "derp," +	//flaps
        "derp," +	//rudder
        "derp," +	//rudder trim
		"derp," +	//speedbrake
        "derp," +	//brake-parking
		"" +	//gear-down
		"\n";
        
        //change just the elevator trim to -0.5
        //expect /controls/flight/elevator-trim to be set to -0.5, other fields retain values
        //result fail- some fields retain values. first protocol fields are zeroed out while 
        //those after elevator-trim retain values from the previous update
        String fgInputUpdateWithEmptyFieldsPartial = "\n" +
		"," +	//pitot-heat
        "," +	//window-heat
		"," +	//wing-heat
        "," +	//battery-switch
        "," +	//mixture
        "," +	//throttle
        "," +	//aileron
        "," +	//aileron trim
        "," +	//auto-coordination
        "," +	//auto-coordination factor
        "," +	//elevator
        "-0.5," +	//elevator trim
        "\n";

        System.out.println("First control write");
        
		// static method in other poc class
		ControlInputWrite.performWrite(host, inputPort, fgInput);

		try {
			Thread.sleep(20L * 1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Second control write");
		
		ControlInputWrite.performWrite(host, inputPort, fgInputUpdateFull);
		
		try {
			Thread.sleep(20L * 1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Update control write");
		
		//ControlInputWrite.performWrite(host, inputPort, fgInputUpdateEmptyFields);
		//ControlInputWrite.performWrite(host, inputPort, fgInputUpdateEmptyFieldsPartial);
		//ControlInputWrite.performWrite(host, inputPort, fgInputUpdateWithSpacedFields);
		ControlInputWrite.performWrite(host, inputPort, fgInputUpdateWithJunkFields);
	}
    
    /*	c172p control typically on port 5004 for C172P Alpha
    
     <!-- control -->
     <chunk>
        <node>/controls/anti-ice/pitot-heat</node>
        <name>pitot-heat</name>
        <type>bool</type>
     </chunk>
     <chunk>
        <node>/controls/anti-ice/window-heat</node>
        <name>window-heat</name>
        <type>bool</type>
     </chunk>
     <chunk>
        <node>/controls/anti-ice/wing-heat</node>
        <name>wing-heat</name>
        <type>bool</type>
     </chunk>
     <chunk>
        <node>/controls/electric/battery-switch</node>
        <name>battery-switch</name>
        <type>bool</type>
     </chunk>
     <chunk>
        <node>/controls/engines/current-engine/mixture</node>
        <name>mixture</name>
        <type>float</type>
     </chunk>
     <chunk>
        <node>/controls/engines/current-engine/throttle</node>
        <name>throttle</name>
        <type>float</type>
     </chunk>
     <chunk>
        <node>/controls/flight/aileron</node>
        <name>aileron</name>
        <type>float</type>
     </chunk>
     <chunk>
        <node>/controls/flight/aileron-trim</node>
        <name>aileron-trim</name>
        <type>float</type>
     </chunk>
     <chunk>
        <node>/controls/flight/auto-coordination</node>
        <name>auto-coordination</name>
        <type>bool</type>
     </chunk>
     <chunk>
        <node>/controls/flight/auto-coordination-factor</node>
        <name>auto-coordination-factor</name>
        <type>float</type>
     </chunk>
     <chunk>
        <node>/controls/flight/elevator</node>
        <name>elevator</name>
        <type>float</type>
     </chunk>
     <chunk>
        <node>/controls/flight/elevator-trim</node>
        <name>elevator-trim</name>
        <type>float</type>
     </chunk>
     <chunk>
        <node>/controls/flight/flaps</node>
        <name>flaps</name>
        <type>float</type>
     </chunk>
     <chunk>
        <node>/controls/flight/rudder</node>
        <name>rudder</name>
        <type>float</type>
     </chunk>
     <chunk>
        <node>/controls/flight/rudder-trim</node>
        <name>rudder-trim</name>
        <type>float</type>
     </chunk>
     <chunk>
        <node>/controls/flight/speedbrake</node>
        <name>speedbrake</name>
        <type>float</type>
     </chunk>
     <chunk>
        <node>/controls/gear/brake-parking</node>
        <name>brake-parking</name>
        <type>float</type>
     </chunk>
     <chunk>
        <node>/controls/gear/gear-down</node>
        <name>gear-down</name>
        <type>bool</type>
     </chunk>
     */
}
