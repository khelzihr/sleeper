package se.cqst.sleeper.parsers;

public interface Parser {
	
	default boolean phraseExists(String phrase, String data)
	{
		return false;
	}

}
