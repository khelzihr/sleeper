package se.cqst.sleeper.parsers;

import java.util.HashMap;

public class PlainTextParser implements Parser {
	
	private HashMap<String, String> arguments;
	
	public PlainTextParser(HashMap<String, String> arguments)
	{
		this.arguments = arguments;
	}
	
	@Override
	public boolean phraseExists(String phrase, String data)
	{
		boolean caseInsensitive = false;
		if(Boolean.parseBoolean(arguments.get("ptp_ci")))
			caseInsensitive = true;
		
		if(caseInsensitive)
			return data.toLowerCase().contains(phrase.toLowerCase());
		else
			return data.contains(phrase);
	}

}
