package se.cqst.sleeper.main;

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
		
		System.out.println(arguments.toString());
		switch(arguments.get("provider"))
		{
		case "guerillamail":
			if(Boolean.valueOf(arguments.get("verbose")))
				System.out.println("Provider set to GuerillaMail");
			provider = new GUMProvider(arguments);
			break;
		case "pop3":
			if(Boolean.valueOf(arguments.get("verbose")))
				System.out.println("Provider set to POP3");
			provider = new POP3Provider(arguments);
			break;
		case "http":
			if(Boolean.valueOf(arguments.get("verbose")))
				System.out.println("Provider set to HTTP");
			provider = new HTTPProvider(arguments);
			break;
		case "console":
			if(Boolean.valueOf(arguments.get("verbose")))
				System.out.println("Provider set to Console");
			provider = new ConsoleProvider(arguments);
			break;
		default:
			if(Boolean.valueOf(arguments.get("verbose")))
				System.out.println("No provider set, returning help");
			MainProgram.printHelp();
			System.exit(0);
			break;
		}
		
		SleeperTask sleeperTask = SleeperTask.getInstance();
		sleeperTask.setProvider(provider);
		sleeperTask.setAction(arguments.get("action"));
		sleeperTask.setVerbose(Boolean.valueOf(arguments.get("verbose")));
		sleeperTask.run();
		
		
	}
	
	public static void printHelp()
	{
		System.out.println();
		System.out.println("sleeper.jar [--keyphrase=phrase] [--action=action] [--provider=provider]");
		System.out.println("            [provider options...]");
		System.out.println();
		System.out.println("Options: ");
		System.out.println("     --keyphrase=phrase   - keyphrase used to execute command");
		System.out.println("     --action=action      - action performed on execution");
		System.out.println("     --provider=provider  - provider sleeper will use to listen");
		System.out.println();
		System.out.println("Provider options: ");
		System.out.println();
		System.out.println("     --provider=guerillamail - Uses GuerillaMail as provider");
		System.out.println("                               An address will be generated every day.");
		System.out.println("     --notify                  Displays current GuerillaMail email address");
		System.out.println("                               for the given keyphrase");
		System.out.println("     --provider=pop3         - Uses POP3 as provider");
		System.out.println("     --pop3server=server     - POP3 server address");
		System.out.println("     --pop3user=user         - POP3 user name");
		System.out.println("     --pop3password=password - POP3 user password");
		System.out.println("     --provider=http         - Uses HTTP/HTTPS as provider");
		System.out.println("     --httpaddress=address   - Address of web page");
		System.out.println("     --provider=console      - Uses the console as provider");
		System.out.println();
		System.out.println("Example: sleeper --keyphrase=\"Test\" --action=\"ping 127.0.0.1\"");
		System.out.println("         --provider=guerillamail");
		System.out.println("Will create a task that listens on a daily generated email address");
		System.out.println("for a speciifc keyphrase");		
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
