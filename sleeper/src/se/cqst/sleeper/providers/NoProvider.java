package se.cqst.sleeper.providers;

import java.util.HashMap;

public class NoProvider implements Provider {
	
	public NoProvider(HashMap<String, String> arguments)
	{
		System.out.println("No provider has been set. Run this application without any arguments to display help.");
		System.out.println("This application will now exit");
		if(Boolean.valueOf(arguments.get("debug")))
			System.out.println(arguments.toString());
		System.exit(0);
	}

}
