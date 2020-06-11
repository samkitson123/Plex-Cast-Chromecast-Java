package org.plexCasting.parsing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.plexCasting.types.PlexDirectory;
import org.plexCasting.types.PlexElement;
import org.plexCasting.types.PlexStream;
import org.plexCasting.utils.WebUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PlexApi 
{
	
	private String address;
	
	private String token;
	
	public void initalise(String plexServerAddress, String token)
	{
		this.address = plexServerAddress;
		this.token = token;
	}
	
	public List<PlexElement> updateLibrary(List<PlexElement> existingElements) throws IOException, SAXException, ParserConfigurationException
	{
		List<PlexElement> changedItems = new ArrayList<>();
		
		List<String> titles = new ArrayList<>();
		
		for(PlexElement element : existingElements)
		{
			titles.add(element.getName());
		}
		
		String content = WebUtils.readInUrl(address + "/library/sections?X-Plex-Token=" + token, 50);
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes("UTF-8"));
		Document doc = builder.parse(input);
		
		Element entity = doc.getDocumentElement();
		
		List<String> libKeys = new ArrayList<String>();
	
		if(entity.getChildNodes().getLength() > 0)
		{			
			NodeList nodes = entity.getChildNodes();
			
			for(int i = 0 ; i < nodes.getLength() ; i++)
			{
				Node child = nodes.item(i);
				
				if(child.getNodeName().equals("Directory"))
				{
					NamedNodeMap att = child.getAttributes();
					
					if(att.getNamedItem("key") != null)
					{
						libKeys.add(att.getNamedItem("key").getNodeValue());
					}
				}
			}
		}
		
		for(String key : libKeys)
		{
			content = WebUtils.readInUrl(address + "/library/sections/" + key + "/all?X-Plex-Token=" + token, 50);
			
			input = new ByteArrayInputStream(content.getBytes("UTF-8"));
			doc = builder.parse(input);
			
			entity = doc.getDocumentElement();
			
			if(entity.getChildNodes().getLength() > 0)
			{
				NodeList nodes = entity.getChildNodes();
				
				for(int i = 0 ; i < nodes.getLength() ; i++)
				{
					Node child = nodes.item(i);
					
					if((child.getNodeName().equals("Directory") || child.getNodeName().equals("Video")) )
					{
						if(child.getAttributes().getNamedItem("title") != null)
						{
							if(!titles.contains(child.getAttributes().getNamedItem("title").getNodeValue()))
							{
								changedItems.add(getElementInfo(child));
							}
						}
					}
				}
			}
		}
		
		
		return changedItems;
	}
	
	
	
	public List<PlexElement> parseLibrary() throws IOException, SAXException, ParserConfigurationException
	{		
		String content = WebUtils.readInUrl(address + "/library/sections?X-Plex-Token=" + token, 50);
	
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes("UTF-8"));
		Document doc = builder.parse(input);
		
		Element entity = doc.getDocumentElement();
		
		List<String> libKeys = new ArrayList<String>();
	
		if(entity.getChildNodes().getLength() > 0)
		{
			NodeList nodes = entity.getChildNodes();
			
			for(int i = 0 ; i < nodes.getLength() ; i++)
			{
				Node child = nodes.item(i);
				
				if(child.getNodeName().equals("Directory"))
				{
					NamedNodeMap att = child.getAttributes();
					
					if(att.getNamedItem("key") != null)
					{
						libKeys.add(att.getNamedItem("key").getNodeValue());
					}
				}
			}
		}
		
		List<PlexElement> elementsToReturn = new ArrayList<PlexElement>();
		
		for(String key : libKeys)
		{
			List<PlexElement> elements = elementsInLibaray(key);
			
			elementsToReturn.addAll(elements);
		}
		
		return elementsToReturn;
	}
	
	private List<PlexElement> elementsInLibaray(String libraryName) throws IOException, SAXException, ParserConfigurationException
	{
		List<PlexElement> results = new ArrayList<PlexElement>();
		
		String content = WebUtils.readInUrl(address + "/library/sections/" + libraryName + "/all?X-Plex-Token=" + token, 50);
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes("UTF-8"));
		Document doc = builder.parse(input);
		
		Element entity = doc.getDocumentElement();
		
		if(entity.getChildNodes().getLength() > 0)
		{
			NodeList nodes = entity.getChildNodes();
			
			for(int i = 0 ; i < nodes.getLength() ; i++)
			{
				Node child = nodes.item(i);
				
				if((child.getNodeName().equals("Directory") || child.getNodeName().equals("Video")))
				{
					
					results.add(getElementInfo(child));
				}
			}
		}
		
		return results;
	}
	
	private PlexElement getElementInfo(Node tagInfo) throws IOException, SAXException, ParserConfigurationException
	{		
		String id = tagInfo.getAttributes().getNamedItem("ratingKey").getNodeValue();
		
		String name = tagInfo.getAttributes().getNamedItem("title").getNodeValue();
		
		if(tagInfo.getNodeName().equals("Directory"))
		{
			PlexDirectory dir = new PlexDirectory(id, name);
			
			List<PlexElement> elements = exploreDirectory(id);
			
			for(PlexElement element : elements)
			{
				dir.addChild(element);
			}
			
			return dir;
		}
		else if(tagInfo.getNodeName().equals("Video"))
		{			
			return exploreStream(id, name);
		}
		else
		{
			//Invalid
			System.out.println("invalid " + tagInfo.getNodeName());
		}
		
		return null;
	}
	
	private List<PlexElement> exploreDirectory(String id) throws IOException, SAXException, ParserConfigurationException
	{
		List<PlexElement> elements = new ArrayList<>();
		
		String content = WebUtils.readInUrl(address + "/library/metadata/" + id + "/children?X-Plex-Token=" + token, 50);
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes("UTF-8"));
		Document doc = builder.parse(input);
		
		Element entity = doc.getDocumentElement();
		
		if(entity.getChildNodes().getLength() > 0)
		{
			NodeList nodes = entity.getChildNodes();
			
			for(int i = 0 ; i < nodes.getLength() ; i++)
			{
				Node child = nodes.item(i);
				
				if(child.getAttributes().getNamedItem("index") == null)
				{
					continue;
				}
				
				child.getAttributes().getNamedItem("index");
				
				if((child.getNodeName().equals("Directory") || child.getNodeName().equals("Video")))
				{
					elements.add(getElementInfo(child));
				}
			}
			
		}
		
		return elements;
	}
	
	public PlexStream exploreStream(String id, String title) throws IOException, SAXException, ParserConfigurationException
	{		
		String content = WebUtils.readInUrl(address + "/library/metadata/" + id + "?X-Plex-Token=" + token, 50);
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes("UTF-8"));
		Document doc = builder.parse(input);
		
		Element entity = doc.getDocumentElement();
		
		if(entity.getChildNodes().getLength() > 0)
		{
			NodeList nodes = entity.getChildNodes();
			
			for(int i = 0 ; i < nodes.getLength() ;i++)
			{
				Node child = nodes.item(i);
				
				if(child.getAttributes().getNamedItem("viewCount") == null)
				{
					continue;
				}
				
				int views = Integer.valueOf(child.getAttributes().getNamedItem("viewCount").getNodeValue());
				
				return new PlexStream(id,title,views);
			}
			
			return new PlexStream(id,title,0);
		}
		
		return null;
	}
	
}
