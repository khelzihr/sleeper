package se.cqst.sleeper.providers;

import java.util.HashMap;

/**
 * <p><strong>NoProvider</strong> is an implementation of the <code>Provider</code> interface that is used when no other
 * <code>Provider</code> implementation has been specified.</p>
 * 
 * @author Nicklas Rosvall Carlquist
 *
 */
public class NoProvider implements Provider {
	
	HashMap<String, String> arguments;
	
	public NoProvider(HashMap<String, String> arguments)
	{
		this.arguments = arguments;
	}
	
	/*
	 * (non-Javadoc)
	 * @see se.cqst.sleeper.providers.Provider#check()
	 * 
	 * When check() is called, an error message will display and the application will exit.
	 */
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
