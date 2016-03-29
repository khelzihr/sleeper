package se.cqst.sleeper.providers;

import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import se.cqst.sleeper.parsers.Parser;

/**
 * The Provider interface provides the application with the tools needed to check if
 * the key phrase has been sent. 
 * 
 * Providers uses the check() method to determine if the
 * key phrase has been found, and may use getParser() to parse incoming text to find
 * the phrase.
 * 
 * @author Nicklas Rosvall Carlquist
 */
public interface Provider {
	
	/**
	 * Check if the key phrase has been provided.
	 *
	 * @return true, if successful
	 * 
	 * @author Nicklas Rosvall Carlquist
	 */
	default boolean check()
	{
		return false;
	}
	
	/**
	 * <p>Use reflection to dynamically instantiate a Parser class based on the arguments
	 * provided.</p>
	 * 
	 * <p>The full name of the class must be provided in the key "parser", ex:
	 * <br />
	 * <code>arguments.set("parser", "se.cqst.sleeper.parsers.PlainTextParser");</code></p>
	 *
	 * @param arguments a HashMap<String, String> with arguments for the Parser
	 * @return the parser
	 * 
	 * @author Nicklas Rosvall Carlquist
	 */
	default Parser getParser(HashMap<String, String> arguments)
	{
		Parser parser = null;
		
		try
		{
			Object instance = null;
			Class<?>	clazz = Class.forName(arguments.get("parser"));
			Constructor<?> constructor = clazz.getConstructor(HashMap.class);
			instance = constructor.newInstance(arguments);
			if(instance instanceof se.cqst.sleeper.parsers.Parser)
				parser = (Parser)instance;
			else
			{
				System.out.println("Provider must be an implementation of se.cqst.sleeper.providers.Parser");
				System.exit(0);
			}
		}
		catch(ClassNotFoundException ex)
		{
			System.out.println("The parser \"" + arguments.get("parser") + 
					"\" does not exist. Make sure you enter the full name of the class.");
			ex.printStackTrace();
			System.exit(0);
		}
		catch(NoSuchMethodException ex)
		{
			System.out.println("The parser \"" + arguments.get("parser") + 
					"\" does not have a valid constructor (valid types are Parser(HashMap<String, String>))");
			ex.printStackTrace();
			System.exit(0);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(0);
		}
		return parser;
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

}
