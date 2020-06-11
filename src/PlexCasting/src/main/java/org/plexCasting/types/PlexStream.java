package org.plexCasting.types;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.plexCasting.model.PlexModel;
import org.plexCasting.utils.PlexCastingUtilities;
import org.xml.sax.SAXException;

import su.litvak.chromecast.api.v2.ChromeCast;


public class PlexStream extends PlexElement
{

	private String streamId;
	
	private String name;
	
	private int viewCount;
	
	public PlexStream(String id, String name, int viewCount)
	{
		super();
		
		this.streamId = id;
		this.name = name;
		this.viewCount = viewCount;
	}
	
	public String getId()
	{
		return streamId;
	}

	public String getName() {
		return name;
	}
	

	public int getViewCount() {
		return viewCount;
	}

	@Override
	public boolean isStream() 
	{
		return true;
	}
	
	public String toString()
	{
		return name + " " + viewCount;
	}

	public void castItem(ChromeCast device) throws SAXException, ParserConfigurationException 
	{
		PlexCastingUtilities.castMedia(this, device);
	}

	@Override
	public void updateViewCount() throws IOException, SAXException, ParserConfigurationException 
	{
		this.viewCount = PlexModel.getInstance().getUpdatedViewCount(getId(), getName());
	}

	@Override
	public PlexElement getCastingId(String id) 
	{
		if(streamId.equals(id))
		{
			return this;
		}
		
		return null;
	}

	@Override
	public String getNameOfId(String id) 
	{
		if(streamId.equals(id))
		{
			return this.name;
		}
		
		return null;
	}

	@Override
	public PlexStream nextToWatch(int currentViews, boolean rootCall) 
	{
		return currentViews > viewCount ? this : null; 
	}

}
