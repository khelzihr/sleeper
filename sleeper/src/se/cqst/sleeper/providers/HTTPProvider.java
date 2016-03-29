package se.cqst.sleeper.providers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import se.cqst.sleeper.parsers.Parser;

/**
 * 
 * <p><code>HTTPProvider</code> is an implementation of the <code>Provider</code> interface that polls a 
 * web site for a specific key phrase, and triggers if it is found.</p>
 * 
 * <p>HTTPProvider will fetch a web page each time a call to <code>{@link #check()}</code> is made and
 * will only parse results with a <code>HTTP 200 OK</code> code.</p>
 * 
 * <p>To parse the web page, a <code>{@link Parser}</code> is used. It is up to the <code>Parser</code>
 * to interpret the web page and decide whether the key phrase exists there or not.</p>
 * 
 * <p><i>A <code>PlainTextParser</code> (default <code>Parser</code>) will look for the key phrase in
 * the entire HTML document. If you use a common enough key phrase this may cause false positives
 * with HTML/CSS/JavaScript code.</i></p>
 * 
 * <p><code>HTTPProvider</code> is initialized with a <code>HashMap&lt;String, String&gt;</code> 
 * containing arguments. The following arguments are used by HTTPProvider (all keys are in the format
 * (String, String) but will be interpreted according to <i>Accepted Value</i>:</p>
 * 
 * <col width="25%" />
 * <col width="25%" />
 * <col width="50%" />
 * <code>
 * 	<table>
 * 		<thead>
 * 			<tr><th>Key</th><th>Accepted value</th><th>Comment</th></tr>
 * 		</thead>
 * 		<tbody>
 * 			<tr><td>keyphrase</td><td>String</td><td>check() returns true if found</td</tr>
 * 			<tr><td>debug</td><td>boolean</td><td>Prints info interesting while debugging</td></tr>
 * 			<tr><td>parser</td><td>Class</td><td>Full class name of a Parser to process messages</td></tr>
 * 		</tbody>
 * 	</table>
 * </code>
 * 
 * @author Nicklas Rosvall Carlquist
 * 
 * @see {@link Provider} for more information about the <code>Provider</code> interface
 * @see {@link Parser} for more information about <code>Parsers</code>
 * 
 * 
 */
public class HTTPProvider implements Provider {
	
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
	
	private HashMap<String, String> arguments;
	private Parser parser;
	
	/**
	 * <p>Instantiate a new <code>HTTPProvider</code> using the provided <code>HashMap&lt;String, String&gt;</code>
	 * with arguments
	 * 
	 * @author Nicklas Rosvall Carlquist
	 * 
	 * @see {@link HTTPParser} for a list of valid arguments
	 *
	 * @param arguments argument list
	 */
	public HTTPProvider(HashMap<String, String> arguments)
	{
		this.arguments = arguments;
		this.parser = this.getParser(arguments);
	}
	
	/* (non-Javadoc)
	 * @see se.cqst.sleeper.providers.Provider#check()
	 * 
	 * The check() method in HTTPProvider calls parseURL(URL url) to determine
	 * if the key phrase exists on the provided web page.
	 */
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
	
	/**
	 * <p>Connects to an input URL object, fetches its data and runs it though <code>this.parser</code></p>
	 *
	 * <p><code>HttpsURLConnection</code> is used to allow for <code>HTTPS</code> connections.</p>
	 * 
	 * <p>Only <code>HTTP 200 OK</code> status code is accepted; pages using e.g. <code> HTTP 3xx codes (
	 * 301 Moved Permanently, 302 Moved)</code> will not be parsed.</p>
	 * 
	 * @param url URL to the web page that should be polled for the key phrase
	 * @return true, if the <code>Parser</code> object finds the key phrase on the page
	 * 
	 * @author Nicklas Rosvall Carlquist
	 * 
	 * @see <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1: Status Code Definitions</a>
	 * for information about HTTP Status Codes
	 * 
	 */
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
