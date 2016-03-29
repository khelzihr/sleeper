package se.cqst.sleeper.providers;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import se.cqst.sleeper.parsers.Parser;

/**
 * <p>GUMProvider is a implementation of the Provider interface that uses the <b>GuerrillaMail</b> API to
 * poll for the key phrase.</p>
 * 
 * <p>GUMProvider will create an e-mail address based on the key phrase and the current date, and
 * listen to it for the key phrase.</p>
 * 
 * <p>A new e-mail address will be generated every day, and users can remotley fetch the new address
 * by running the application with the following arguments: <br /></p>
 * <p><code>provider=se.cqst.sleeper.providers.GUMProvider keyphrase=[phrase] <b>notify</b></code></p>
 * 	 
 * <p>The following keys are used by GUMProvider (all keys are in the format (String, String)
 *  
 * but will be processed according to <i>Accepted Value</i>):</p>
 * <col width="25%" />
 * <col width="25%" />
 * <col width="50%" />
 * <code>
 * 	<table>
 * 		<thead>
 * 			<tr><th>Key</th><th>Accepted value</th><th>Comment</th></tr>
 * 		</thead>
 * 		<tbody>
 * 			<tr><td>notify</td><td>boolean</td><td>Display current e-mail address and halt</td></tr>
 * 			<tr><td>keyphrase</td><td>String</td><td>check() returns true if found</td</tr>
 * 			<tr><td>verbose</td><td>boolean</td><td>Prints more information if set to true</td></tr>
 * 			<tr><td>debug</td><td>boolean</td><td>Prints info interesting while debugging</td></tr>
 * 			<tr><td>parser</td><td>Class</td><td>Full class name of a Parser to process messages</td></tr>
 * 		</tbody>
 * 	</table>
 * </code>
 * 
 * @author Nicklas Rosvall Carlquist
 * 
 * @see <a href="https://www.guerrillamail.com/GuerrillaMailAPI.html">GuerrillaMail API</a> for more information about the API
 */
public class GUMProvider implements Provider {
	
	public static final String API_URL = "http://api.guerrillamail.com/ajax.php";
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
	
	public static final String NOTIFY_INFO = 
			"To trigger a sleeper client using keyprase \"%s\", send an e-mail containing the keyword anywhere in the message " +
			"body to the following address.";
	
	public static final String NOTIFY_INFO2 = "The address changes every day, so run this application again with the " +
			"notify argument to get the current address.";
	
	private HashMap<String, String>	arguments;
	private String lastEmailAddress;
	private Parser parser;
	
	private ObjectMapper mapper;
	
	/**
	 * <p>Instantiate a new GUMProvider using the provided HashMap with arguments.</p>
	 * 
	 * @param arguments a HashMap with arguments
	 * 
	 * @author Nicklas Rosvall Carlquist
	 * 
	 * @see {@link Provider} for general information about the Provider interface
	 * @see {@link GUMProvider} for a list of arguments used by GUMProvider
	 */
	public GUMProvider(HashMap<String, String> arguments)
	{
		this.arguments = arguments;
		this.mapper = new ObjectMapper();
		this.lastEmailAddress = "";
		this.parser = this.getParser(arguments);
		
		this.printUsage();
		
		if(Boolean.valueOf(arguments.get("notify")))
		{
			System.out.println(String.format(NOTIFY_INFO, arguments.get("keyphrase")));
			System.out.println();
			System.out.println(this.doInitializeGUM().getEmail_addr());
			System.out.println();
			System.out.println(NOTIFY_INFO2);
			System.exit(0);
		}
	}
	
	/* (non-Javadoc)
	 * @see se.cqst.sleeper.providers.Provider#check()
	 * 
	 * The check() method in GUMProvider initializes a 
	 * GuerrillaMailboxObject, fetches emails and calls
	 * on parseEmails() to determine if the key phrase is found
	 */
	@Override
	public boolean check()
	{
		GuerrillaMailboxObject object = this.doInitializeGUM();
		object = this.doFetchEmails(this.doGetEmailList(object));
		return this.parseEmails(object);
	}
	
	/**
	 * <p>Parses e-mails in a GuerrillaMailboxObject for a specific keyword</p>
	 * 
	 * <p>When the body of e-mails has been downloaded, parseEmails() can be used
	 * to determine if the key phrase exist within any of the e-mails.</p>
	 * 
	 * <p>Since GuerrillaMail always keep a Welcome Message mail in a new inbox with
	 * email_id=1, this message is excluded.</p>
	 * 
	 * <p>A Parser is used to process the messages, and is specified in the key <code>parser</code>
	 * provided in the argument HashMap</p>
	 *
	 * @param object the GuerrillaMailboxObject
	 * @return true, if key phrase is found by the Parser
	 * 
	 * @author Nicklas Rosvall Carlquist
	 * 
	 * @see {@link Parser} for more information about Parsers
	 */
	private boolean parseEmails(GuerrillaMailboxObject object)
	{
		if(object == null || object.getList() == null || object.getList().isEmpty())
			return false;
		else
		{
			for(GuerrillaMailboxObject.GuerrillaMailObject mail : object.getList())
			{
				int mailId = 0;
				try
				{
					mailId = Integer.parseInt(mail.getMail_id());
				}
				catch(NumberFormatException ex)
				{
					
				}
				if(mailId != 1)
				{
					if(Boolean.valueOf(arguments.get("debug")))
						print("Debug: Parsing mail: " + mail.getMail_id());
					if(this.parser.phraseExists(arguments.get("keyphrase"), mail.getMail_body()))
						return true;
				}
			}
		}
		
		if(Boolean.valueOf(arguments.get("verbose")))
			print("Verbose: Keyphrase was not found");
		
		return false;
	}
	
	/**
	 * <p>Initializes a <code>GuerrillaMailboxObject</code> by calling function 
	 * <code>get_email_address</code> at the GuerrillaMail API.</p>
	 * 
	 * <p>This method generates a GuerrillaMailboxObject with a default e-mail address
	 * and a valid <code>sid_token</code></p>
	 *
	 * @return A new GuerrillaMailboxObject with a default e-mail address
	 * 
	 * @author Nicklas Rosvall Carlquist
	 * 
	 * @see {@link #queryGuerrillaMail(String, String, GuerrillaMailboxObject, boolean)} for more information about the API
	 * @see {@link GuerrillaMailboxObject} for information about the object
	 */
	private GuerrillaMailboxObject doGetEmailAddress()
	{
		return this.queryGuerrillaMail("?f=get_email_address", null, null, false);
	}
	
	
	/**
	 * <p>Take an existing GuerrillaMailboxObject with a valid <code>sid_token</code> and 
	 * set the e-mail address to one specified by the method</p>
	 * 
	 * <p>E-mail address is generated by {@link #getMd5EmailAddress()}.
	 *
	 * @param object the object
	 * @return the input object with a specified e-mail address
	 * 
	 * @author Nicklas Rosvall Carlquist
	 */
	private GuerrillaMailboxObject doSetEmailUser(GuerrillaMailboxObject object)
	{
		object = this.queryGuerrillaMail("?f=set_email_user", null, object, true);
		if(!object.getEmail_addr().equals(this.lastEmailAddress))
		{
			print("GUMProvider now listening on: ");
			print(object.getEmail_addr());
			this.lastEmailAddress = object.getEmail_addr();
		}
		return object;
	}
	
	
	/**
	 * <p>Take an existing GuerrillaMailboxObject with a valid <code>sid_token</code> 
	 * and retrieve a list of the first 20 e-mail messages into it.</p>
	 * 
	 * <p>This method does not fetch the message body of the e-mails, and if there
	 * are more than 20 e-mails, only the first 20 will be fetched.</p>
	 * 
	 * 
	 *
	 * @param object A GuerrillaMailboxObject with a valid <code>sid_token</code>
	 * @return The input object updated with a list of e-mails.
	 * 
	 * @author Nicklas Rosvall Carlquist
	 */
	private GuerrillaMailboxObject doGetEmailList(GuerrillaMailboxObject object)
	{
		return this.queryGuerrillaMail("?f=get_email_list", null, object, true);
	}
	
	/**
	 * <p>Tries to fetch the message body of <code>GuerrillaMailObjects</code> stored within
	 * the input <code>GuerrillaMailboxObject</code>.</p>
	 * 
	 * <p>Each <code>GuerrillaMailObject</code> is processed and it's body requested.</p>
	 *
	 * @param object The input GuerrillaMailboxObject with a list of messages
	 * @return The input object with added message bodies
	 * 
	 * @author Nicklas Rosvall Carlquist
	 */
	private GuerrillaMailboxObject doFetchEmails(GuerrillaMailboxObject object)
	{
		if(object.getList() != null)
		{
			for(GuerrillaMailboxObject.GuerrillaMailObject mail : object.getList())
			{
				int mailId = 0;
				try
				{
					mailId = Integer.parseInt(mail.getMail_id());
				}
				catch(NumberFormatException ex)
				{
					
				}
				if(mailId > 1)
				{
					URL address = null;
					try
					{
						address = new URL(API_URL + "?f=fetch_email&sid_token=" + object.getSid_token() + "&email_id=" + mail.getMail_id());
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
						System.exit(0);
					}
					URLConnection connection = null;
					try
					{
						connection = address.openConnection();
						connection.setRequestProperty("User-Agent", USER_AGENT);
						connection.connect();
						ObjectReader objr = mapper.readerForUpdating(mail);
						objr.readValue(connection.getInputStream());
						connection.getInputStream().close();
					}
					catch (Exception ex)
					{
						System.out.println("Could not read mail_id: " + mail.getMail_id());
					}
				}
			}
		}
		
		return object;
	}
	
	/**
	 * <p>Queries the GuerrillaMail API with the specified <code>function</code>.</p>
	 * 
	 * <p>The GuerrillaMail API uses JSON objects to communicate, and this method creates
	 * the URL of the API function call, fetches the JSON object and parses it.</p>
	 * 
	 * <p><code>parameters</code> can either be provided or left empty (null); if left empty,
	 * default parameters exist for 
	 * <code><ul><li>get_email_address</li><li>set_email_user</li><li>get_email_list</li></ul></code></p> 
	 * 
	 * <p>If <code>doUpdate</code> is set to <code>true</code>, the provided <code>object</code> will be updated
	 * and returned. If set to <code>false</code>, a new <code>GuerrillaMailboxObject</code> will be returned.</p>
	 * 
	 * <p>GuerrillaMail requires that a User-Agent is set when connecting to their server, and the const 
	 * <code>GUMProvider.API_URL</code> is used for this.</p>
	 *
	 * @param function The function to call, all functions begin with "?f=". See API documentation for more info
	 * @param parameters The parameters the function requires.
	 * @param object A <code>GuerrillaMailboxObject</code>, or <code>null</code> if no input object is required.
	 * @param doUpdate <code>true</code> if you are updating an existing <code>GuerrillaMailboxObject</code>. <code>false</code> otherwise.
	 * @return A <code>GuerrillaMailboxObject</code> containing the returned JSON data.
	 * 
	 * @author Nicklas Rosvall Carlquist
	 * 
	 * @see {@link GUMProvider} for more information about the GuerrillaMail API and links to further resources.
	 */
	private GuerrillaMailboxObject queryGuerrillaMail(String function, String parameters, GuerrillaMailboxObject object, boolean doUpdate)
	{
		URL address = null;
		
		if(parameters == null || parameters.equals(""))
		{
			switch(function)
			{
			case "?f=get_email_address":
				parameters = "&ip=" + this.getLocalIPAddress() + "&agent=sltest";
				break;
			case "?f=set_email_user":
				if(object != null && doUpdate)
					parameters = "&email_user=" + getMd5EmailAddress() + "&sid_token=" + object.getSid_token();
				else
					parameters = "";
				break;
			case "?f=get_email_list":
				if(object != null && doUpdate)
					parameters = "&sid_token=" + object.getSid_token() + "&offset=0";
				else
					parameters = "";
				break;
			default:
				parameters = "";
				function = "";
				break;
			}
		}
		
		try 
		{
			address = new URL(API_URL + function + parameters);
		} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
			System.exit(0);
		}
		
		URLConnection connection;
		try 
		{
			connection = address.openConnection();
			connection.setRequestProperty("User-Agent", USER_AGENT);
			connection.connect();
			if(doUpdate)
			{
				ObjectReader objr = mapper.readerForUpdating(object);
				objr.readValue(connection.getInputStream());
			}
			else
			{
				object = mapper.readValue(connection.getInputStream(), GUMProvider.GuerrillaMailboxObject.class);
			}
			connection.getInputStream().close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(0);
		}	
		
		
		return object;
	}
	

	/**
	 * <p>Return an object that has been initialized with a valid <code>sid_token</code>
	 * and set a valid e-mail address</p>
	 *
	 * @return A <code>GuerrillaMailboxObject</code> with a valid <code>sid_token</code> and a valid e-mail address
	 * 
	 * @author Nicklas Rosvall Carlquist
	 */
	private GuerrillaMailboxObject doInitializeGUM()
	{
		return doSetEmailUser(doGetEmailAddress());
	}
	
	
	/**
	 * <p>Returns an MD5 hash to be used as e-mail address</p>
	 * 
	 * <p>The MD5 hash consists of:
	 * <code><ul><li>A salt</li><li>The key phase</li><li>Todays date (yyyyMMdd)</li>
	 * </ul></li></code></p>
	 *
	 * @return A MD5 hashed String
	 * 
	 * @author Nicklas Rosvall Carlquist
	 */
	private String getMd5EmailAddress()
	{		
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		Calendar cal = Calendar.getInstance();
		String output = ("sl;" + arguments.get("keyphrase") + ";" + df.format(cal.getTime()));
		return this.getMd5(output);
	}
	
	/**
	 * <p>Generate a MD5 hash from a String</p>
	 *
	 * @param input input String
	 * @return MD5 hash of the String
	 * 
	 * @author Nicklas Rosvall Carlquist
	 */
	private String getMd5(String input)
	{
		String hashtext = "";
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(input.getBytes());
			BigInteger number = new BigInteger(1, digest);
            hashtext = number.toString(16);
            while (hashtext.length() < 32)
            {
                hashtext = "0" + hashtext;
            }
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return hashtext;
	}
	
	/**
	 * <p>Returns the local IP-address; if multiple addresses, one will be chosen.</p>
	 *
	 * @return the local ip address
	 * 
	 * @author Nicklas Rosvall Carlquist
	 */
	private String getLocalIPAddress()
	{
		String ipAddress = "192.168.0.2";
		try
		{
			 String[] tempAddress = InetAddress.getLocalHost().toString().split("/");
			 if(tempAddress.length > 1)
				 ipAddress=tempAddress[1];
			 else
				 ipAddress=tempAddress[0];
		}
		catch (UnknownHostException e) {}
		
		return ipAddress;
		
	}
	
	/* (non-Javadoc)
	 * @see se.cqst.sleeper.providers.Provider#printHelp()
	 * 
	 * Override the default printHelp implementation and print
	 * specific help for GUMProvider.
	 * 
	 * Call the Parsers printHelp() function too.
	 */
	@Override
	public void printHelp()
	{
		print("Provider GUMProvider listens to a GuerrillaMail e-mail address that is re-generated every day. The current address"
				+ "can be generated from another instance by using the argument \"notify\" with the same keyphrase (case sensitive!)");
		
		if(this.parser != null)
			parser.printHelp();
	}
	
	/* (non-Javadoc)
	 * @see se.cqst.sleeper.providers.Provider#printUsage()
	 * 
	 * Override printUsage() and display information about
	 * the current configuration of GUMProvider.
	 * 
	 * Also call the function printUsage() on this.parser
	 */
	@Override
	public void printUsage()
	{
		print("GUMProvider will be used and will listen to a generated e-mail address for the incoming keyprase.");
		if(Boolean.valueOf(this.arguments.get("notify")))
			print("Argument notify has been set and GUMProvider will only display the current valid e-mail address for the"
					+ "given keyphrase (note that the keyphrase is case sensitive)");
		
		if(this.parser != null)
			parser.printUsage();
	}
	
	/**
	 * <p>The GuerrillaMailboxObject is the Java equivalent to the JSON objects used by the
	 * GuerrillaMail API.</p>
	 * 
	 * <p>GUMProvider uses the <code>com.fasterxml.jackson</code> library to process JSON data into <code>GuerrillaMailboxObjects</code>.
	 * This dictates the layout of the <code>GuerrillaMailboxObject</code> class, since it's get/set methods needs to be compilant with
	 * jackson processing.</p>
	 * 
	 * <p>With <code>JsonIgnoreProperties(ignoreUnknown = true)</code>, jackson should still be able to process JSON data
	 * from GuerrillaMail into <code>GuerrillaMailboxObjects</code> if another JSON field is added to the API.</p>
	 * 
	 * @author Nicklas Rosvall Carlquist
	 *  
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class GuerrillaMailboxObject
	{
		private String email_addr;
		private String lang;
		private String sid_token;
		private String domain;
		private Integer email_timestamp;
		private String alias;
		private String alias_error;
		private String site_id;
		private String site;
		private Integer count;
		private List<GuerrillaMailObject> list;
		
		
		/**
		 * <p>Instantiates a new guerrilla mailbox object.</p>
		 * 
		 * <p>All data is filled using the setter methods, so this constructor does
		 * nothing.</p>
		 * 
		 * @author Nicklas Rosvall Carlquist
		 */
		public GuerrillaMailboxObject()
		{
			
		}
		
		@Override
		public String toString()
		{
			return  " alias: " + alias +
					"\r\n alias_error: " + alias_error + 
					"\r\n count: " + count +
					"\r\n domain: " + domain +
					"\r\n email_addr: " + email_addr + 
					"\r\n email_timestamp: " + email_timestamp +
					"\r\n lang: " + lang + 
					"\r\n list: " + ((list != null) ? list.toString() : "") +
					"\r\n sid_token: " + sid_token +
					"\r\n site: " + site +
					"\r\n site_id: " + getSite_id();
					
		}
		
		//	Getters and setters
		public String 	getEmail_addr() 								{	return email_addr;	}
		public void 	setEmail_addr(String email_addr) 				{	this.email_addr = email_addr;	}
		public String 	getLang() 										{	return lang;	}
		public void 	setLang(String lang) 							{	this.lang = lang;	}
		public String 	getSid_token() 									{	return sid_token;	}
		public void 	setSid_token(String sid_token) 					{	this.sid_token = sid_token;	}
		public String 	getDomain() 									{	return domain;	}
		public void 	setDomain(String domain) 						{	this.domain = domain;	}
		public Integer 	getEmail_timestamp()							{	return email_timestamp;	}
		public void 	setEmail_timestamp(Integer email_timestamp)		{	this.email_timestamp = email_timestamp;	}
		public String 	getAlias()										{	return alias;	}
		public void 	setAlias(String alias)							{	this.alias = alias;	}
		public String 	getAlias_error() 								{	return alias_error;	}
		public void 	setAlias_error(String alias_error) 				{	this.alias_error = alias_error;	}
		public String 	getSite_id() 									{	return site_id;	}
		public void 	setSite_id(String site_id) 						{	this.site_id = site_id;		}
		public String 	getSite() 										{	return site;	}
		public void 	setSite(String site) 							{	this.site = site;	}		
		public Integer 	getCount() 										{	return count;	}
		public void 	setCount(Integer count) 						{	this.count = count;		}
		public List<GuerrillaMailObject> getList() 						{	return list;	}
		public void setList(List<GuerrillaMailObject> list) 			{	this.list = list;	}

		/**
		 * <p>The <code>GuerrillaMailObject</code> is a representation of 
		 * the e-mails fetched from GuerrillaMail as JSON objects in Java.</p>
		 * 
		 * <p>This class is used by <code>com.fasterxml.jackson</code> to translate
		 * JSON objects into Java class instances.</p>
		 * 
		 * @author Nicklas Rosvall Carlquist
		 * 
		 * @see {@link GUMProvider.GuerrillaMailboxObject} for information about classes used by <code>jackson</code>
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class GuerrillaMailObject
		{
			
			private String mail_id;
			private String mail_from;
			private String mail_recipient;
			private String mail_subject;
			private String mail_excerpt;
			private String mail_body;
			private String mail_timestamp;
			private String mail_date;
			private Integer mail_read;
			private String content_type;
			private String sid_token;
			
			@Override
			public String toString()
			{
				return " mail_id: " + mail_id +
						"\r\n mail_from: " + mail_from + 
						"\r\n mail_recipient: " + mail_recipient + 
						"\r\n mail_subject: " + mail_subject +
						"\r\n mail_excerpt: " + mail_excerpt +
						"\r\n mail_body: " + mail_body +
						"\r\n mail_timestamp: " + mail_timestamp +
						"\r\n mail_date: " + mail_date +
						"\r\n mail_read: " + mail_read + 
						"\r\n content_type: " + content_type +
						"\r\n sid_token: " + sid_token;
			}
		
			//	getters and setters
			public String 	getMail_id() 								{	return mail_id;		}
			public void 	setMail_id(String mail_id) 					{	this.mail_id = mail_id;		}
			public String 	getMail_from() 								{	return mail_from;	}
			public void 	setMail_from(String mail_from) 				{	this.mail_from = mail_from;	}
			public String 	getMail_recipient() 						{	return mail_recipient;	}
			public void 	setMail_recipient(String mail_recipient) 	{	this.mail_recipient = mail_recipient;	}
			public String 	getMail_subject() {	return mail_subject;	}
			public void 	setMail_subject(String mail_subject) 		{	this.mail_subject = mail_subject;	}
			public String 	getMail_excerpt() 							{	return mail_excerpt;	}
			public void 	setMail_excerpt(String mail_excerpt) 		{	this.mail_excerpt = mail_excerpt;	}
			public String 	getMail_body() 								{	return mail_body;	}
			public void 	setMail_body(String mail_body) 				{	this.mail_body = mail_body;	}
			public String 	getMail_timestamp() 						{	return mail_timestamp;	}
			public void 	setMail_timestamp(String mail_timestamp) 	{	this.mail_timestamp = mail_timestamp;	}
			public String 	getMail_date() 								{	return mail_date;	}
			public void 	setMail_date(String mail_date) 				{	this.mail_date = mail_date;	}
			public Integer 	getMail_read() 								{	return mail_read;	}
			public void 	setMail_read(Integer mail_read) 			{	this.mail_read = mail_read;	}
			public String 	getContent_type() 							{	return content_type;	}
			public void 	setContent_type(String content_type)		{	this.content_type = content_type;	}
			public String 	getSid_token() 								{	return sid_token;	}
			public void 	setSid_token(String sid_token) 				{	this.sid_token = sid_token;	}

		}
		
		
	}
	
}
