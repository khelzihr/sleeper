package se.cqst.sleeper;

public class MainProgram {

	public static void main(String[] args) {
		
		switch(args.length)
		{
		case 0:
		default:
			MainProgram.printHelp();
			System.exit(0);
			break;
		}
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

}
