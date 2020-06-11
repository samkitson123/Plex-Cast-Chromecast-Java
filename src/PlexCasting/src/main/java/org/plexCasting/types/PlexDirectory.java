package org.plexCasting.types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.plexCasting.utils.PlexCastingUtilities;
import org.xml.sax.SAXException;

import su.litvak.chromecast.api.v2.ChromeCast;

public class PlexDirectory extends PlexElement 
{
    private List<PlexElement> children;
	
	private String directoryId;
	
	private String name;
	
	public PlexDirectory(String directoryId, String name)
	{
		super();
		
		children = new ArrayList<PlexElement>();
		
		this.directoryId = directoryId;
		
		this.name = name;
	}
	
	public void addChild(PlexElement element)
	{
		children.add(element);
	}
	
	public List<PlexElement> getChildren()
	{
		return new ArrayList<>(children);
	}
	
	public String getId()
	{
		return this.directoryId;
	}
	
	public String getName()
	{
		return name;
	}
	
	public PlexStream nextToWatch(int currentViews, boolean rootCall)
	{	
		int upperBound = currentViews;
		
		if(rootCall)
		{
			upperBound = 999;
		}
		
		for(int i = currentViews ; i <= upperBound; i++)
		{
			for(PlexElement child : children)
			{
				PlexStream stream = child.nextToWatch(i,false);
				
				if(stream != null)
				{
					System.out.println(stream.getName() + " " + stream.getId());
					
					return stream;
				}
			}
		}
		
		return null;
		
	}
	

	@Override
	public boolean isStream() 
	{
		return false;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder(name + ":\n");
		
		for(PlexElement element : children)
		{
			sb.append(element.toString());
		}
		
		return sb.toString();
	}

	public void castItem(ChromeCast device) throws SAXException, ParserConfigurationException 
	{
		List<String> logs = new ArrayList<>();
		
		try 
		{
			updateViewCount();
		} catch (IOException | SAXException | ParserConfigurationException e) 
		{
			logs.add("Unable to update views going to try continue, plex is most likely offline");
			e.printStackTrace();
		}
		
		PlexCastingUtilities.castMedia(nextToWatch(0,true), device);
	}

	@Override
	public void updateViewCount() throws IOException, SAXException, ParserConfigurationException 
	{
		for(PlexElement element : children)
		{
			element.updateViewCount();
		}
	}

	@Override
	public PlexElement getCastingId(String id) 
	{
		if(directoryId.equals(id))
		{
			return this;
		}
		
		for(PlexElement child : children)
		{
			PlexElement result = child.getCastingId(id);
			
			if(result != null)
			{
				return result;
			}
		}
		
		return null;
	}

	@Override
	public String getNameOfId(String id) 
	{
		if(directoryId.equals(id))
		{
			return this.name;
		}
		
		for(PlexElement child : children)
		{
			String result = child.getNameOfId(id);
			
			if(result != null)
			{
				return this.name + " - " + result;
			}
		}
		
		return null;
	}

}
