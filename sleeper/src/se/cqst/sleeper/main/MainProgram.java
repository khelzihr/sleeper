package se.cqst.sleeper.main;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import se.cqst.sleeper.SleeperTask;
import se.cqst.sleeper.providers.*;

/**
 * The MainProgram class parses arguments and launches a SleeperTask.
 * 
 * @author Nicklas Rosvall Carlquist
 *
 */
public class MainProgram {

	/**
	 * Parse incoming arguments and create a SleeperTask
	 * @param args Input arguments
	 */
	public static void main(String[] args) 
	{
		//	Create arguments HashMap by passing args to getArguments()
		HashMap<String, String>	arguments = MainProgram.getArguments(args);
		
		//	Create empty HashMap to use for comparison
		HashMap<String, String> emptyArgs = new HashMap<String, String>();
		
		//	Initialize emptyArgs to default values
		MainProgram.initialize(emptyArgs);
		
		//	If arguments are empty, print usage information and exit application
		if(arguments.equals(emptyArgs))
		{
			MainProgram.printHelp();
			System.exit(0);
		}
		
		//	Dynamically create ProviderImpl object
		Provider provider = MainProgram.getProvider(arguments);
		
		//Print all arguments if debug is set
		if(Boolean.valueOf(arguments.get("debug")))
			System.out.println(arguments.toString());
		
		SleeperTask sleeperTask = SleeperTask.getInstance();
		
		//	Set default repeat interval to 5 minutes
		int repeat = 5;
		
		//	Try to get custom repeat interval
		try
		{
			repeat = Integer.parseInt(arguments.get("repeat"));
		}
		catch(NumberFormatException ex)
		{ }
		
		//	Set SleeperTask verbose status based on arguments
		sleeperTask.setVerbose(Boolean.valueOf(arguments.get("verbose")));
		
		//	Set SleeperTask repeat to repeat
		sleeperTask.setRepeat(repeat);
		
		//	Set SleeperTask provider
		sleeperTask.setProvider(provider);
		
		//	Set SleeperTask action
		sleeperTask.setAction(arguments.get("action"));

		//	Start SleeperTask
		sleeperTask.run();
		
		
	}
	
	/**
	 * Print help. 
	 * 
	 */
	public static void printHelp()
	{
		System.out.println("Sleeper - TODO: Write help section");
	}
	
	/**
	 * Extract arguments from a String array
	 * @param args Input String array
	 * @return HashMap&lt;String, String&gt; with arguments
	 */
	private static HashMap<String, String> getArguments(String[] args)
	{
		HashMap<String, String>	arguments = new HashMap<String, String>();
		MainProgram.initialize(arguments);
		for(String argument : args)
		{
			String[] splitString = argument.split("=", 2);
			if(splitString.length > 1)
				arguments.put(splitString[0], splitString[1]);
			else
				arguments.put(splitString[0], "true");
		}
		return arguments;
	}
	
	/**
	 * Try to instantiate a {@link Provider} from a HashMap of arguments.
	 * 
	 * The argument value of key "provider" must be a FQDN of the Provider implementation.
	 * 
	 * @param arguments HashMap of arguments
	 * @return Provider
	 */
	private static Provider getProvider(HashMap<String, String> arguments)
	{
		Provider provider = null;
		
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
				System.out.println("Provider must be an implementation of se.cqst.sleeper.providers.Provider");
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
		return provider;
	}
	
	/**
	 * Add (or set if already exists) default values for keys in a HashMap of arguments
	 * @param arguments Input arguments HashMap
	 */
	private static void initialize(HashMap<String, String> arguments)
	{
		arguments.put("keyphrase", "");
		arguments.put("action", "");
		arguments.put("provider", "se.cqst.sleeper.providers.NoProvider");
		arguments.put("notify", "false");
		arguments.put("pop3server", "");
		arguments.put("pop3user", "");
		arguments.put("pop3password", "");
		arguments.put("httpaddress", "");
		arguments.put("verbose", "false");
		arguments.put("debug", "false");
		arguments.put("parser", "se.cqst.sleeper.parsers.PlainTextParser");
		arguments.put("repeat", "5");
	}

}
