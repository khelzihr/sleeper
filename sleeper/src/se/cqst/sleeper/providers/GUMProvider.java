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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class GUMProvider implements Provider {
	
	public static final String API_URL = "http://api.guerrillamail.com/ajax.php";
	
	// public static final String GUERRILLA_DOMAIN = "grr.la";
	
	public static final String NOTIFY_INFO = 
			"To trigger a sleeper client using keyprase \"%s\", send an e-mail containing the keyword anywhere in the message " +
			"body to the following address.";
	
	public static final String NOTIFY_INFO2 = "The address changes every day, so run this application again with the " +
			"notify argument to get the current address.";
	
	private HashMap<String, String>	arguments;
	
	private ObjectMapper mapper;
	
	public GUMProvider(HashMap<String, String> arguments)
	{
		this.arguments = arguments;
		this.mapper = new ObjectMapper();
		
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
		System.out.println(doInitializeGUM());
		return false;
	}
	
	private GuerrillaMailObject doGetEmailAddress()
	{
		URL address = null;
		String ipAddress = this.getLocalIPAddress();
		GUMProvider.GuerrillaMailObject object = null;

		try 
		{
			address = new URL(API_URL + "?f=get_email_address&ip=" + ipAddress + "&agent=sltest");
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
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			connection.connect();
			object = mapper.readValue(connection.getInputStream(), GUMProvider.GuerrillaMailObject.class);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(0);
		}
		return object;
	}
	
	private GuerrillaMailObject doSetEmailAddress(GuerrillaMailObject object)
	{
		URL address = null;
		
		try 
		{
			address = new URL(API_URL + "?f=set_email_user" + "&email_user=" + getMD5EmailAddress() + 
					"&sid_token=" + object.getSid_token());
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
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			connection.connect();
			/*
			Scanner sc = new Scanner(connection.getInputStream());
			sc.useDelimiter("\\A");
			System.out.println(sc.next());
			sc.close();
			*/
			ObjectReader objr = mapper.readerForUpdating(object);
			objr.readValue(connection.getInputStream());
		} 
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(0);
		}
		return object;
	}
	

	private GuerrillaMailObject doInitializeGUM()
	{
		return doSetEmailAddress(doGetEmailAddress());
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
	
	@SuppressWarnings("unused")
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
	public static class GuerrillaMailObject
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
		
		public GuerrillaMailObject()
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
					"\r\n domain: " + domain +
					"\r\n email_addr: " + email_addr + 
					"\r\n email_timestamp: " + email_timestamp +
					"\r\n lang: " + lang + 
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

		
		
		
	}

}
