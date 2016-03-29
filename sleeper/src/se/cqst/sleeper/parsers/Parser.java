package se.cqst.sleeper.parsers;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public interface Parser {
	
	default boolean phraseExists(String phrase, String data)
	{
		return false;
	}
	
	/**
	 * <p>Returns the current date</p>
	 *
	 * @return Current date in yyyy-MM-dd HH:mm format
	 * 
	 * @author Nicklas Rosvall Carlquist
	 */
	default String getDate()
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return sdf.format(cal.getTime());
	}
	
	/**
	 * <p>Prints text to <code>System.out</code> with a prefix date in brackets.</p>
	 *
	 * @param text text to be printed
	 * 
	 * @author Nicklas Rosvall Carlquist
	 */
	default void print(String text)
	{
		System.out.println("[" + getDate() + "] " + text);
	}
	
	default void	printHelp()
	{
		print("This Parser has no help specified.");
	}
	
	default void	printUsage()
	{
		print("This Parser has no usage information defined.");
	}

}
