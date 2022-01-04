package org.jason.flightgear.flight.util;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jason.flightgear.flight.position.TrackPosition;
import org.jason.flightgear.flight.position.WaypointPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Log waypoints and trackpoints and write a gpx file of a flight. Resulting gpx files can be very large 
 * for long flights and not every mapping tool has a large size limit. This one 
 * seems to work for larger ones http://www.mygpsfiles.com/app/
 * 
 * @author jason
 *
 */
public class FlightLog {

	private final static Logger LOGGER = LoggerFactory.getLogger(FlightLog.class);
	
	private static final int DEFAULT_TP_SIZE = 500000;
	private static final int DEFAULT_WP_SIZE = 100;
	
	private ArrayList<WaypointPosition> waypoints;
	private ArrayList<TrackPosition> trackPositions;
	
	public FlightLog() {
		waypoints = new ArrayList<>(DEFAULT_WP_SIZE);
		trackPositions = new ArrayList<>(DEFAULT_TP_SIZE);
	}
	
	public void addWaypoint(WaypointPosition p) {
		waypoints.add(p);
	}
	
	public void addWaypoints(ArrayList<WaypointPosition> positions) {
		waypoints.addAll(positions);
	}
	
	public void addTrackPosition(TrackPosition p) {
		trackPositions.add(p);
	}
	
	public void writeGPXFile(String filename) {
		writeGPXFile( System.getProperty("user.dir"), filename );
	}
	
	public void writeGPXFile(String path, String filename) {
		
		//create path if it doesn't exist
		
		
		
		LOGGER.info("Writing flight log to {}", filename);
		
		try {
	        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
	        
	        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
	
	        Document document = documentBuilder.newDocument();
	        
	        // root gpx element
	        Element root = document.createElement("gpx");
	        document.appendChild(root);
	        
	        // metadata
	        Element metadata= document.createElement("metadata");
	        //set attributes
	        
	        root.appendChild(metadata);
	        
	        //route - rte
	        Element route = document.createElement("rte");
	        
	        //established waypoints for route
	        waypoints.forEach( (n) -> generateWaypointNode(n, route) );
	        
	        root.appendChild(route);
	        
	        //route - trk
	        Element track = document.createElement("trk");
	        Element trackSeg = document.createElement("trkseg");
	        
	        //track points
	        //optionally maybe write every 4th route point if we have a lot of points
	        //online gpx mapping tools have low file size limits
	        trackPositions.forEach( (n) -> generateTrackNode(n, trackSeg) );
			
	        track.appendChild(trackSeg);
	        
			
			root.appendChild(track);
			
			//write the file
	        // create the xml file
	        //transform the DOM Object to an XML File
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        Transformer transformer = transformerFactory.newTransformer();
	        DOMSource domSource = new DOMSource(document);
	        StreamResult streamResult = new StreamResult(new File(filename));
	        
	        transformer.transform(domSource, streamResult);
        } catch (ParserConfigurationException | TransformerException e) {
            LOGGER.error("Exception writing gpx file", e);
        } 
		
		LOGGER.info("Flight log written sucessfully");
	}

	private void generateTrackNode(TrackPosition n, Element parent) {
		
		Element trackPointElement = parent.getOwnerDocument().createElement("trkpt");
		trackPointElement.setAttribute("lat", String.valueOf(n.getLatitude()));
		trackPointElement.setAttribute("lon", String.valueOf(n.getLongitude()));

		//altitude
		Element elevationElement = trackPointElement.getOwnerDocument().createElement("ele");
		elevationElement.setTextContent(String.valueOf(n.getAltitude()));
		
		Element timeElement = trackPointElement.getOwnerDocument().createElement("time");
		timeElement.setTextContent(String.valueOf(n.getTime()));
		
		trackPointElement.appendChild(elevationElement);
		trackPointElement.appendChild(timeElement);
		
		parent.appendChild(trackPointElement);
	}
	
	private void generateWaypointNode(WaypointPosition n, Element parent) {
		
		Element waypointElement = parent.getOwnerDocument().createElement("rtpt");
		waypointElement.setAttribute("lat", String.valueOf(n.getLatitude()));
		waypointElement.setAttribute("lon", String.valueOf(n.getLongitude()));

		//altitude
		Element elevationElement = waypointElement.getOwnerDocument().createElement("ele");
		elevationElement.setTextContent(String.valueOf(n.getAltitude()));
		
		Element nameElement = waypointElement.getOwnerDocument().createElement("name");
		nameElement.setTextContent(String.valueOf(n.getName()));
		
		waypointElement.appendChild(elevationElement);
		waypointElement.appendChild(nameElement);
		
		parent.appendChild(waypointElement);
	}

}
