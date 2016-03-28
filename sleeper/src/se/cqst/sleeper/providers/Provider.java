package se.cqst.sleeper.providers;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import se.cqst.sleeper.parsers.Parser;

public interface Provider {
	
	default boolean check()
	{
		return false;
	}
	
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
				System.out.println("Provider must be an implementation of se.cqst.sleeper.providers.Provider");
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
