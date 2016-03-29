package se.cqst.sleeper.providers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import se.cqst.sleeper.parsers.Parser;

public class HTTPProvider implements Provider {
	
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
	
	private HashMap<String, String> arguments;
	private Parser parser;
	
	public HTTPProvider(HashMap<String, String> arguments)
	{
		this.arguments = arguments;
		this.parser = this.getParser(arguments);
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
		String data = "";
		try
		{
			HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
			connection.addRequestProperty("User-Agent", USER_AGENT);	
			
			if(connection.getResponseCode() != 200)
			{
				print(connection.getResponseCode() + connection.getResponseMessage() + " when trying to access " + url.toString());
			}
			else
			{
				Scanner sc = new Scanner(connection.getInputStream());
				sc.useDelimiter("\\A");
				data = (sc.hasNext() ? sc.next() : "");
				if(Boolean.valueOf(arguments.get("debug")))
					System.out.println(data);
				sc.close();
			}
			connection.getInputStream().close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(0);
		}
		
		return this.parser.phraseExists(arguments.get("keyphrase"), data);
	}

}
