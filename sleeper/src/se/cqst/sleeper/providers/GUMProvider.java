package se.cqst.sleeper.providers;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class GUMProvider implements Provider {
	
	public static final String NOTIFY_INFO = 
			"To trigger a sleeper client using keyprase \"%s\", send an e-mail containing the keyword anywhere in the message " +
			"body to the following address.";
	
	public static final String NOTIFY_INFO2 = "The address changes every day, so run this application again with the " +
			"notify argument to get the current address.";
	
	HashMap<String, String>	arguments;
	
	public GUMProvider(HashMap<String, String> arguments)
	{
		this.arguments = arguments;
		
		if(Boolean.valueOf(arguments.get("notify")))
		{
			System.out.println(String.format(NOTIFY_INFO, arguments.get("keyphrase")));
			System.out.println();
			System.out.println(this.getEmailAddress());
			System.out.println();
			System.out.println(NOTIFY_INFO2);
			System.exit(0);
		}
	}
	
	@Override
	public boolean check()
	{
		String apiurl = "http://api.guerrillamail.com/ajax.php";
		String ipAddress = "192.168.0.2";
		try
		{
			 ipAddress = InetAddress.getLocalHost().toString();
		}
		catch (UnknownHostException e) {}
		
		
		
		
		
		return false;
	}
	
	private void checkEmailAddress(HashMap<String, String> arguments)
	{
		
	}
	
	private String getEmailAddress()
	{		
		SimpleDateFormat df = new SimpleDateFormat("YYYYMMdd");
		Calendar cal = Calendar.getInstance();
		String output = ("sl;" + arguments.get("keyphrase") + ";" + df.format(cal.getTime()));
		return this.getMd5(output) + "@grr.la";
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

}
