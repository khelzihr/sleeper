package se.cqst.sleeper.providers;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import se.cqst.sleeper.parsers.Parser;

/**
 * The Provider interface provides the application with the tools needed to check if
 * the key phrase has been sent. 
 * 
 * Providers uses the check() method to determine if the
 * key phrase has been found, and may use getParser() to parse incoming text to find
 * the phrase.
 */
public interface Provider {
	
	/**
	 * Check if the key phrase has been provided.
	 *
	 * @return true, if successful
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

}
