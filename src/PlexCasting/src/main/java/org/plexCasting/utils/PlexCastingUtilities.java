package org.plexCasting.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.plexCasting.types.PlexElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import com.chromecast.addedContent.LOAD;
import com.chromecast.addedContent.PlexMedia;

import su.litvak.chromecast.api.v2.ChromeCast;
import su.litvak.chromecast.api.v2.Media.StreamType;

public class PlexCastingUtilities 
{
	private static final int TIMEOUT_SECONDS = 30;
	
	private static boolean initalised = false;
	
	private static String plexQueueScriptLocation;
	
	private static String serverAddress;
	
	private static String token;
	
	private static String username;
	
	private static String portNumber;
	
	public static void initalise(String queueScriptLocation, String serverAddressI, String tokenI, String usernameI, String portNumberI)
	{
		initalised = true;
		
		plexQueueScriptLocation = queueScriptLocation;
		
		serverAddress = serverAddressI;
		
		portNumber = portNumberI;
		
		token = tokenI;
		
		username = usernameI;
	}
	
	public static void castMedia(PlexElement elementToCast, ChromeCast chromecast) throws SAXException, ParserConfigurationException
	{		
		// Check initialised
		if(!initalised)
		{
			throw new RuntimeException("Please initalise plex casting utilities first!");
		}
		
		ChromeCast toCastTo = chromecast;
		
		System.out.println("Chromecast found: " + toCastTo.getTitle() + " IP: " + toCastTo.getAddress());
		
		String plexIp = "https://" + serverAddress + ":" + portNumber;
		
		try 
		{
			// Launch the plex app
			toCastTo.connect();
			toCastTo.launchApp("9AC194DC");
			
			// Define the user
			Map<String,Object> user = new HashMap<>();
			user.put("username",username);
			
			// Define the server details
			Map<String,Object> server = new HashMap<>();
			
			// Gets the machine identifier of the plex server
			server.put("machineIdentifier", getMachineIdentifier(plexIp,token));
			
			// Set default metadata
			server.put("transcoderVideo", true);
			server.put("transcoderVideoRemuxOnly", false);
			server.put("transcoderAudio", false);
			server.put("version", "1.18.4.2171");
			server.put("myPlexSubscription", false);
			server.put("isVerifiedHostname", true);
			
			// Set server information
			server.put("protocol", "https");
			server.put("address", serverAddress);
			server.put("port", String.valueOf(portNumber));
			
			// Request transient token
			server.put("accessToken", getTransientToken(plexIp,token));
			
			// Check the requests retrieved a value
			if(server.get("machineIdentifier").equals(""))
			{
				System.out.println("Unable to get the machine identifier");
				return;
			}
			else if(server.get("accessToken").equals(""))
			{
				System.out.println("Unable to get the transient token");
				return;
			}
			
			Map<String,Object> custom = new HashMap<>();
			
			// Default metadata
			custom.put("playQueueType","video");
			custom.put("providerIdentifier","com.plexapp.plugins.library");
			custom.put("offset", 0);
			custom.put("directPlay", true);
			custom.put("directStream", true);
			
			// This can remain static as just used as an interface ID
			custom.put("playbackSessionID", "aaaaaaaaaaaaaaaaaaaaaaa");
			
			//Default metadata
			custom.put("subtileColor", "#ffffff");
			custom.put("subtilePosition", "bottom");
			custom.put("repeat",0);
			custom.put("audioForceMutliChannel", false);
			custom.put("debugAdKind", 0);
			custom.put("subtileSize", 100);
			custom.put("audioBoost", 100);
			custom.put("debugAdKind", 0);
			
			// Set server and user info
			custom.put("server", server);
			custom.put("user", user);
			
			// Retrieve play queue information
			custom.put("containerKey", "/playQueues/" + createPlayQueue(elementToCast,plexIp,token) + "");

			// Check requests were valid
			if(custom.get("containerKey").equals("/playQueues/-1"))
			{
				System.out.println("Unable to get the play queue");
				return;
			}
			
			int timer = 0;
			
			// Check that plex is running on the chromecast
			while(!toCastTo.getRunningApp().name.equals("Plex") && toCastTo.getRunningApp().isIdleScreen)
			{
				// Timeout to ensure this doesn't last forever 
				if(timer > TIMEOUT_SECONDS)
				{					
					System.out.println("Timed out waiting for the plex app to launch");
					
					return;
				}
				
				timer++;
				
				// Wait to try again
				try 
				{
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) 
				{
					// If this fails it will just loop till a timeout occurs
					System.out.println("Sleep failed: " + e.getMessage());
				}
			}

			// Create a media item to be sent to the chromecast
			PlexMedia media = new PlexMedia("/library/metadata/"+elementToCast.getId(),"video/mp4",StreamType.BUFFERED,custom);
			
			// Double check plex is running	        
	        if (!toCastTo.getRunningApp().name.equals("Plex")) 
	        {
	            System.out.println("Plex is not running on the cast device - must have been changed");
		
	            return;
	        }
	        
	        // Cast to the chromecast
			toCastTo.send("urn:x-cast:com.google.cast.media",new LOAD(toCastTo.getRunningApp().sessionId,media,true,0,custom));
			
		} catch (IOException | GeneralSecurityException e) 
		{
			System.out.println("Cast Failed: " + e.getMessage());
		}
		
		return;
	}
	
	private static String getTransientToken(String serverAddress, String serverToken) throws SAXException, IOException, ParserConfigurationException
	{
		String url = serverAddress + "/security/token?type=delegation&scope=all&X-Plex-Token=" + serverToken;
		String result="";
		try {
			result = WebUtils.readInUrl(url, 100);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		ByteArrayInputStream input = new ByteArrayInputStream(result.getBytes("UTF-8"));
		Document doc = builder.parse(input);
		
		Element entity = doc.getDocumentElement();
		
		NamedNodeMap att = entity.getAttributes();
		
		if(att.getNamedItem("token") != null)
		{
			return att.getNamedItem("token").getNodeValue();
		}
		
		System.out.println("Failed to get the transient token, got this instead: \"" + result + "\"");
		
		return "";
	}
	
	private static int createPlayQueue(PlexElement element, String serverAddress, String token)
	{
		String python = "python";
		
		String[] cmd = new String[] {python,plexQueueScriptLocation,"--url="+serverAddress, "--access_token=" + token, "--mediaID=" + element.getId()};
		
		String command = cmd[0];
		
		for(String item : cmd)
		{
			if(item.equals(cmd[0]))
			{
				continue;
			}
			
			command = command + " " + item;
		}
		
		System.out.println(command);

		StringBuilder sb = new StringBuilder();
		
		try 
		{
			Process process = Runtime.getRuntime().exec(cmd);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			
			String line = "";
			
			while(process.isAlive())
			{
				try {
					TimeUnit.MILLISECONDS.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			while((line = reader.readLine()) != null)
			{
				sb.append(line);
				System.out.println(line);
			}
		} 
		catch (IOException e) 
		{
			System.out.println("Unable to retrieve the result of the script, therefore unable to confirm it has completed: " + e.getMessage());
			return -1;
		}
		
		try 
		{
			return Integer.valueOf(sb.toString().trim());
		}
		catch(NumberFormatException e)
		{
			System.out.println("Failed to get the queue number, got this instead: \"" + sb.toString().trim() + "\"");
			
			return -1;
		}
	}
	
	private static String getMachineIdentifier(String address, String token) throws SAXException, IOException, ParserConfigurationException
	{
		String result = "";
		
		try {
			result = WebUtils.readInUrl(address + "/?X-Plex-Token=" +token , 100);
		} 
		catch (IOException e1) 
		{
			System.out.println("Failed to get the machine identifier, " + e1.getMessage());
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		ByteArrayInputStream input = new ByteArrayInputStream(result.getBytes("UTF-8"));
		Document doc = builder.parse(input);
		
		Element entity = doc.getDocumentElement();
		
		if(entity.getAttributes().getNamedItem("machineIdentifier") != null)
		{
			return entity.getAttributes().getNamedItem("machineIdentifier").getNodeValue();
		}

		System.out.println("Failed to get the Machine Identifier, got this instead: \"" + result + "\"");
		
		return "";
	}
	
	

}
