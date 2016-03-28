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

public class GUMProvider implements Provider {
	
	public static final String API_URL = "http://api.guerrillamail.com/ajax.php";
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
	
	// public static final String GUERRILLA_DOMAIN = "grr.la";
	
	public static final String NOTIFY_INFO = 
			"To trigger a sleeper client using keyprase \"%s\", send an e-mail containing the keyword anywhere in the message " +
			"body to the following address.";
	
	public static final String NOTIFY_INFO2 = "The address changes every day, so run this application again with the " +
			"notify argument to get the current address.";
	
	private HashMap<String, String>	arguments;
	private String lastEmailAddress;
	
	private ObjectMapper mapper;
	
	public GUMProvider(HashMap<String, String> arguments)
	{
		this.arguments = arguments;
		this.mapper = new ObjectMapper();
		this.lastEmailAddress = "";
		
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
	
	@Override
	public boolean check()
	{
		GuerrillaMailboxObject object = this.doInitializeGUM();
		object = this.doFetchEmails(this.doGetEmailList(object));
		return this.parseEmails(object);
	}
	
	private boolean parseEmails(GuerrillaMailboxObject object)
	{
		Parser parser = this.getParser(arguments);
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
					if(parser.phraseExists(arguments.get("keyphrase"), mail.getMail_body()))
						return true;
				}
			}
		}
		
		if(Boolean.valueOf(arguments.get("verbose")))
			print("Verbose: Keyphrase was not found");
		
		return false;
	}
	
	private GuerrillaMailboxObject doGetEmailAddress()
	{
		return this.queryGuerrillaMail("?f=get_email_address", null, null, false);
	}
	
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
	
	private GuerrillaMailboxObject doGetEmailList(GuerrillaMailboxObject object)
	{
		return this.queryGuerrillaMail("?f=get_email_list", null, object, true);
	}
	
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
					parameters = "&email_user=" + getMD5EmailAddress() + "&sid_token=" + object.getSid_token();
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
	

	private GuerrillaMailboxObject doInitializeGUM()
	{
		return doSetEmailUser(doGetEmailAddress());
	}
	
	private String getMD5EmailAddress()
	{		
		SimpleDateFormat df = new SimpleDateFormat("YYYYMMdd");
		Calendar cal = Calendar.getInstance();
		String output = ("sl;" + arguments.get("keyphrase") + ";" + df.format(cal.getTime()));
		return this.getMd5(output);
	}
	
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
	
	private String getDate()
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return sdf.format(cal.getTime());
	}
	
	private void print(String text)
	{
		System.out.println("[" + getDate() + "] " + text);
	}
	
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
		
		public GuerrillaMailboxObject()
		{
			this.email_addr = "";
			this.lang = "";
			this.sid_token = "";
			this.domain = "";
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
		
		public String getEmail_addr() 
		{
			return email_addr;
		}
		public void setEmail_addr(String email_addr) 
		{
			this.email_addr = email_addr;
		}
		public String getLang() 
		{
			return lang;
		}
		public void setLang(String lang) 
		{
			this.lang = lang;
		}
		public String getSid_token() 
		{
			return sid_token;
		}
		public void setSid_token(String sid_token) 
		{
			this.sid_token = sid_token;
		}
		public String getDomain() 
		{
			return domain;
		}
		public void setDomain(String domain) 
		{
			this.domain = domain;
		}

		public Integer getEmail_timestamp()
		{
			return email_timestamp;
		}
		public void setEmail_timestamp(Integer email_timestamp)
		{
			this.email_timestamp = email_timestamp;
		}
		public String getAlias()
		{
			return alias;
		}
		public void setAlias(String alias)
		{
			this.alias = alias;
		}
		
		public String getAlias_error() {
			return alias_error;
		}
		public void setAlias_error(String alias_error) {
			this.alias_error = alias_error;
		}
		public String getSite_id() {
			return site_id;
		}
		public void setSite_id(String site_id) {
			this.site_id = site_id;
		}
		
		public String getSite() {
			return site;
		}
		public void setSite(String site) {
			this.site = site;
		}		

		public Integer getCount() {
			return count;
		}

		public void setCount(Integer count) {
			this.count = count;
		}

		public List<GuerrillaMailObject> getList() {
			return list;
		}

		public void setList(List<GuerrillaMailObject> list) {
			this.list = list;
		}

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
		
			public String getMail_id() {
				return mail_id;
			}
			public void setMail_id(String mail_id) {
				this.mail_id = mail_id;
			}
			public String getMail_from() {
				return mail_from;
			}
			public void setMail_from(String mail_from) {
				this.mail_from = mail_from;
			}
			public String getMail_recipient() {
				return mail_recipient;
			}
			public void setMail_recipient(String mail_recipient) {
				this.mail_recipient = mail_recipient;
			}
			public String getMail_subject() {
				return mail_subject;
			}
			public void setMail_subject(String mail_subject) {
				this.mail_subject = mail_subject;
			}
			public String getMail_excerpt() {
				return mail_excerpt;
			}
			public void setMail_excerpt(String mail_excerpt) {
				this.mail_excerpt = mail_excerpt;
			}
			public String getMail_body() {
				return mail_body;
			}
			public void setMail_body(String mail_body) {
				this.mail_body = mail_body;
			}
			public String getMail_timestamp() {
				return mail_timestamp;
			}
			public void setMail_timestamp(String mail_timestamp) {
				this.mail_timestamp = mail_timestamp;
			}
			public String getMail_date() {
				return mail_date;
			}
			public void setMail_date(String mail_date) {
				this.mail_date = mail_date;
			}
			public Integer getMail_read() {
				return mail_read;
			}
			public void setMail_read(Integer mail_read) {
				this.mail_read = mail_read;
			}
			public String getContent_type() {
				return content_type;
			}
			public void setContent_type(String content_type) {
				this.content_type = content_type;
			}
			public String getSid_token() {
				return sid_token;
			}
			public void setSid_token(String sid_token) {
				this.sid_token = sid_token;
			}

		}
		
		
	}
	
}
