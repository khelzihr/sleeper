package se.cqst.sleeper.providers;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class GUMProvider implements Provider {
	
	HashMap<String, String>	arguments;
	
	public GUMProvider(HashMap<String, String> arguments)
	{
		this.arguments = arguments;
		System.out.println(getMd5("Test"));
		System.out.println(getEmailAddress());
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

}
