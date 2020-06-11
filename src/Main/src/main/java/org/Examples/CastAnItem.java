package org.Examples;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.plexCasting.model.PlexModel;
import org.plexCasting.types.PlexDirectory;
import org.plexCasting.types.PlexElement;
import org.xml.sax.SAXException;

import su.litvak.chromecast.api.v2.ChromeCast;
import su.litvak.chromecast.api.v2.ChromeCasts;

public class CastAnItem {
	
	// Configurable settings - please change these before running for your configuration
	private static final String chromecastName = "CHROMECAST_NAME";
	
	private static final String pythonCreatePlexQueueScript = "C:\\path\\to\\PlexCreateQueue.py";

	private static final String plexServerIP = "192.168.fill.in";
	
	private static final String plexServerAccessToken = "PLEX_SERVER_ACCESS_TOKEN";
	
	private static final String plexServerPort = "32400";
	
	private static final String plexUsername = "PLEX_USER_NAME";
	
	
	public static void main(String[] args) throws SAXException, ParserConfigurationException, IOException 
	{
		// Start Looking for Chromecasts
		ChromeCasts.startDiscovery();
		
		// Initialise the Plex Model with the correct settings
		System.out.println("Start");
		System.out.println("Updating Library...");
		PlexModel.getInstance().initalise(pythonCreatePlexQueueScript, plexServerIP, plexServerAccessToken, plexUsername, plexServerPort);
		
		// Obtain the contents of the plex server
		List<PlexElement> elements = PlexModel.getInstance().getPlexElements();
		
		// Go through the elements recursively and print the item names
		for(PlexElement child : elements)
		{
			explorePlexElements(child,0);
		}
		
		// Placeholder for the found chromecast
		ChromeCast device = null;
		
		// Get all chromecasts found on the network
		for(ChromeCast chromecast : ChromeCasts.get())
		{
			System.out.println("Device found - Name: \"" + chromecast.getTitle() + "\"");
			
			// If the chromecast has the same name as the one being searched for, keep it
			if(chromecast.getTitle().equals(chromecastName))
			{
				device = chromecast;
			}
			
		}
		
		// Check if a chromecast was found
		if(device == null)
		{
			throw new RuntimeException("Unable to find device with name \"" + chromecastName + "\"");
		}
		
		// Cast the first item in the model, for an example
		PlexElement elementToCast = elements.get(0);
		System.out.println("Casting the first item found - Casting: " + elementToCast.getName());
		elementToCast.castItem(device);
	}
	
	public static void explorePlexElements(PlexElement element, int level)
	{
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0 ; i < level; i++)
		{
			sb.append("   ");
		}
		
		sb.append(element.getName());
		System.out.println(sb.toString());
		
		if(element instanceof PlexDirectory)
		{
			PlexDirectory dir = (PlexDirectory) element;
			for(PlexElement child : dir.getChildren())
			{
				explorePlexElements(child,level + 1);
			}
		}
	}

}
