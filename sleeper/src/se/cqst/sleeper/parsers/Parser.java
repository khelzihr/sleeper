package se.cqst.sleeper.parsers;

public interface Parser {
	
	default boolean phraseExists(String data)
	{
		return false;
	}

}
