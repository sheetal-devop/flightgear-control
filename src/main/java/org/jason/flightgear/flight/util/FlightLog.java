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

import org.jason.flightgear.flight.waypoints.WaypointPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Log waypoints and write a gpx file of a flight. Resulting gpx files can be very large 
 * for long flights and not every mapping tool has a large size limit. This one 
 * seems to work for larger ones http://www.mygpsfiles.com/app/
 * 
 * @author jason
 *
 */
public class FlightLog extends ArrayList<WaypointPosition> {

	private final static Logger LOGGER = LoggerFactory.getLogger(FlightLog.class);
	
	private static final long serialVersionUID = -53420201634907576L;
	
	public FlightLog() {
		super(10000);
	}
	
	public void writeGPXFile(String filename) {
		
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
	        
	        //route points
	        //optionally maybe write every 4th route point if we have a lot of points
	        //online gpx mapping tools have low file size limits
			this.forEach( (n) -> generateRoutePointNode(n, route) );
			
			root.appendChild(route);
			
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

	private void generateRoutePointNode(WaypointPosition n, Element parent) {
		
		Element routePointElement = parent.getOwnerDocument().createElement("rtept");
		routePointElement.setAttribute("lat", String.valueOf(n.getLatitude()));
		routePointElement.setAttribute("lon", String.valueOf(n.getLongitude()));
		
		parent.appendChild(routePointElement);
	}

}
