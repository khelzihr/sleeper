package se.cqst.sleeper.providers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class HTTPProvider implements Provider {
	
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
	
	HashMap<String, String> arguments;
	
	public HTTPProvider(HashMap<String, String> arguments)
	{
		this.arguments = arguments;
	}
	
	@Override
	public boolean check()
	{
		URL address = null;
		try
		{
			address = new URL(arguments.get("httpaddress"));
		}
		catch(MalformedURLException ex)
		{
			print("The specified URL \"" + arguments.get("httpaddress") + "\" is not a valid URL");
			ex.printStackTrace();
			System.exit(0);
		}
		
		
		return parseURL(address);
	}
	
	private boolean parseURL(URL url)
	{
		try
		{
			HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
			connection.addRequestProperty("User-Agent", USER_AGENT);

			//	Parse
			
			if(connection.getResponseCode() != 200)
			{
				print(connection.getResponseCode() + connection.getResponseMessage() + " when trying to access " + url.toString());
			}
			connection.getInputStream().close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

}
