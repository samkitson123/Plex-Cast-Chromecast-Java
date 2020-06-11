package org.plexCasting.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.plexCasting.parsing.PlexApi;
import org.plexCasting.types.PlexElement;
import org.plexCasting.utils.PlexCastingUtilities;
import org.xml.sax.SAXException;

import su.litvak.chromecast.api.v2.ChromeCasts;

public class PlexModel
{
	private PlexApi api;
	
	private List<PlexElement> elements = new ArrayList<>();
	
	private static String serverAddress;
	
	private static String token;
	
	private static String portNumber;
	
	private static PlexModel instance = new PlexModel();
	
	private PlexModel() 
	{
		super();
	}
	
	public static PlexModel getInstance()
	{
		return instance;
	}

	protected PlexElement getPlexElement(String id) 
	{
		for(PlexElement item : elements)
		{			
			PlexElement result = item.getCastingId(id);
			
			if(result != null)
			{
				return result;
			}
		}
		
		return null;
	}
	
	public String getFullName(String id)
	{
		for(PlexElement item : elements)
		{			
			String result = item.getNameOfId(id);
			
			if(result != null)
			{
				return result;
			}
		}
		
		return null;
	}

	public void initalise(String queueScriptLocation, String serverAddressI, String tokenI, String usernameI, String portNumberI) 
	{
		PlexCastingUtilities.initalise(queueScriptLocation, serverAddressI, tokenI, usernameI, portNumberI);
		
		try {
			ChromeCasts.startDiscovery();
		} catch (IOException e) 
		{
			e.printStackTrace();
			//TODO log
		}
		
		
		serverAddress = serverAddressI;
		
		portNumber = portNumberI;
		
		token = tokenI;
		
		if(elements.size() == 0)
		{
			refreshLibrary();
		}
		else
		{
			updateLibrary();
		}
	}
	
	public void updateLibrary()
	{
		api = new PlexApi();
		
		api.initalise("https://" + serverAddress + ":" + portNumber, token);
		
		List<PlexElement> changedItems = null;
		try {
			changedItems = api.updateLibrary(elements);
		} catch (IOException e) 
		{
			System.out.println("Failed to update Plex Library, " + e.getMessage());
			return;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CHANGED_LOOP:
		for(PlexElement element : changedItems)
		{
			
			for(PlexElement existingElement : elements)
			{
				if(existingElement.getId().equals(existingElement.getId()))
				{
					elements.remove(existingElement);
					elements.add(element);
					continue CHANGED_LOOP;
				}
			}
			
			elements.add(element);
		}
		
		System.out.println("Added " + changedItems.size() + " plex elements to the library");
		
		
	}
	
	public void refreshLibrary()
	{
		api = new PlexApi();
		
		api.initalise("https://" + serverAddress + ":" + portNumber, token);
		
		try 
		{
			elements.addAll(api.parseLibrary());
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	} 
	
	public int getUpdatedViewCount(String id, String title) throws IOException, SAXException, ParserConfigurationException
	{
		return api.exploreStream(id, title).getViewCount();
	}
	
	public List<PlexElement> getPlexElements()
	{
		return elements;
	}
	
}
