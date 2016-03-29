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
	
	/**
	 * <p>Print detailed information about how you configure and use the Parser</p>
	 * 
	 * <p>Example: <i>Parser x reads incoming data backwards and looks for the keyphrase.
	 * For key phrase <code>Foo</code>, ParserX.phraseExists() would return <code>true</code> if <code>ooF</code>
	 * exists in the input data.</i></p>
	 * 
	 * <p><i>Valid arguments are: argumentA, argumentB, argumentC<br />
	 * argumentA - do something<br />
	 * argumentB - do something else<br />
	 * argumentC - do something different</i></p>
	 * 
	 * @author Nicklas Rosvall Carlquist
	 */
	default void	printHelp()
	{
		print("This Parser has no help specified.");
	}
	
	/**
	 * <p>Print information about what the Parser will do. This information will be
	 * displayed when you run the application with the specified Provider</p>
	 * 
	 * <p>Example: <i>Parser x will be used to find the specified key phrase by reading the
	 * input data backwards. ArgumentA has been specified and parser x will do something.</i></p>
	 * 
	 * @author Nicklas Rosvall Carlquist
	 */
	default void	printUsage()
	{
		print("This Parser has no usage information defined.");
	}

}
