package se.cqst.sleeper.providers;

public interface Provider {
	
	default boolean check()
	{
		return false;
	}

}
