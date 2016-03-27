package se.cqst.sleeper.providers;

import java.util.HashMap;

public class NoProvider implements Provider {
	
	HashMap<String, String> arguments;
	
	public NoProvider(HashMap<String, String> arguments)
	{
		this.arguments = arguments;
	}
	
	@Override
	public boolean check()
	{
		System.out.println("No provider has been set. Run this application without any arguments to display help.");
		System.out.println("This application will now exit");
		if(Boolean.valueOf(this.arguments.get("debug")))
			System.out.println(this.arguments.toString());
		System.exit(0);
		return false;
	}

}
