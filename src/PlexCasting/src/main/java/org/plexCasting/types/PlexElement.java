package org.plexCasting.types;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import su.litvak.chromecast.api.v2.ChromeCast;


public abstract class PlexElement
{
	
	public PlexElement() 
	{
		super();
	}

	public abstract boolean isStream();
	
	public abstract String getId();
	
	public abstract String getName();
	
	public abstract void updateViewCount() throws IOException, SAXException, ParserConfigurationException;
	
	public abstract void castItem(ChromeCast device) throws SAXException, ParserConfigurationException;
	
	public abstract PlexElement getCastingId(String id);
	
	public abstract String getNameOfId(String id);
	
	public abstract PlexStream nextToWatch(int currentViews, boolean rootCall);

}
