package se.cqst.sleeper.main;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import se.cqst.sleeper.SleeperTask;
import se.cqst.sleeper.providers.*;

public class MainProgram {

	public static void main(String[] args) 
	{
		HashMap<String, String>	arguments = new HashMap<String, String>();
		Provider provider = null;
		MainProgram.initialize(arguments);
		for(String argument : args)
		{
			String[] splitString = argument.split("=", 2);
			if(splitString.length > 1)
				arguments.put(splitString[0], splitString[1]);
			else
				arguments.put(splitString[0], "true");
		}
		
		try
		{
			Object instance = null;
			Class<?>	clazz = Class.forName(arguments.get("provider"));
			Constructor<?> constructor = clazz.getConstructor(HashMap.class);
			instance = constructor.newInstance(arguments);
			if(instance instanceof se.cqst.sleeper.providers.Provider)
				provider = (Provider)instance;
			else
			{
				System.out.println("Provider must be an implementation of se.cqst.sleeper.providers");
				System.exit(0);
			}
		}
		catch(ClassNotFoundException ex)
		{
			System.out.println("The provider \"" + arguments.get("provider") + 
					"\" does not exist. Make sure you enter the full name of the class.");
			ex.printStackTrace();
			System.exit(0);
		}
		catch(NoSuchMethodException ex)
		{
			System.out.println("The provider \"" + arguments.get("provider") + 
					"\" does not have a valid constructor (valid types are Provider(HashMap<String, String>))");
			ex.printStackTrace();
			System.exit(0);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(0);
		}
		
		System.out.println(arguments.toString());
		
		SleeperTask sleeperTask = SleeperTask.getInstance();
		sleeperTask.setProvider(provider);
		sleeperTask.setAction(arguments.get("action"));
		sleeperTask.setVerbose(Boolean.valueOf(arguments.get("verbose")));
		sleeperTask.run();
		
		
	}
	
	public static void printHelp()
	{
		//TODO: Rewrite help
	}
	
	private static void initialize(HashMap<String, String> arguments)
	{
		arguments.put("keyphrase", "");
		arguments.put("action", "");
		arguments.put("provider", "");
		arguments.put("notify", "false");
		arguments.put("pop3server", "");
		arguments.put("pop3user", "");
		arguments.put("pop3password", "");
		arguments.put("httpaddress", "");
		arguments.put("verbose", "false");
	}

}
