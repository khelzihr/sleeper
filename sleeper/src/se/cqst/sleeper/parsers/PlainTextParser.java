package se.cqst.sleeper.parsers;

import java.util.HashMap;

/**
 * <p><code>PlainTextParser</code> is the default implementation of the <code>Parser</code> interface,
 * and parses all input data as plain text.</p>
 * 
 * <p>The <code>PlainTextParser</code> compares the input data with the key phrase like two Strings.
 * It is by default case sensitive, but can be made case insensitive with the attribute <code>ptp_ci</code></p>
 * 
 * <p>A <code>PlainTextParser</code> object is created by passing a <code>HashMap&lt;String, String&gt;</code> 
 * with arguments to it. The following is a list of arguments used by the Parser (note that all arguments are in
 * the format (String, String), but the column <i>Accepted Value</i> displays how they are interpreted):</p>
 * 
 * <col width="25%" />
 * <col width="25%" />
 * <col width="50%" />
 * <code>
 * 	<table>
 * 		<thead>
 * 			<tr><th>Key</th><th>Accepted value</th><th>Comment</th></tr>
 * 		</thead>
 * 		<tbody>
 * 			<tr><td>ptp_ci</td><td>boolean</td><td>if set, compares input and key phrase case insensitive</td</tr>
 * 		</tbody>
 * 	</table>
 * </code>
 * 
 * 
 * @author Nicklas Rosvall Carlquist
 * 
 */
public class PlainTextParser implements Parser {
	
	private HashMap<String, String> arguments;
	
	/**
	 * Instantiate a new <code>PlainTextParser</code> using the provided <code>HashMap</code>
	 *
	 * @param arguments HashMap with arguments
	 */
	public PlainTextParser(HashMap<String, String> arguments)
	{
		this.arguments = arguments;
	}
	
	/* (non-Javadoc)
	 * @see se.cqst.sleeper.parsers.Parser#phraseExists(java.lang.String, java.lang.String)
	 * 
	 * Override default implementation and compare the two input Strings to each other.
	 * 
	 * If String phrase is found within String data, return true.
	 * 
	 * If argument ptp_ci is set, compare case insensitive.
	 */
	@Override
	public boolean phraseExists(String phrase, String data)
	{
		boolean caseInsensitive = false;
		if(Boolean.parseBoolean(arguments.get("ptp_ci")))
			caseInsensitive = true;
		
		if(caseInsensitive)
			return data.toLowerCase().contains(phrase.toLowerCase());
		else
			return data.contains(phrase);
	}
	
	/* (non-Javadoc)
	 * @see se.cqst.sleeper.parsers.Parser#printHelp()
	 * 
	 * Override default printHelp() and print help regarding PlainTextParser
	 */
	@Override
	public void printHelp()
	{
		print("Parser PlainTextParser parses incoming data as plain text and compares it to the provided keyphrase. The"
				+ "PlainTextParser treats potential metadata and tags as text, and will trigger the action if the keyphrase"
				+ "is found anywhere in the data provided by a Provider. PlainTextParser can use argument ptp_ci to compare"
				+ "text case insensitive. Note that this ONLY affects the parsing of data - any provider that relies on the keyphrase"
				+ " (e.g. to generate an e-mail address) will not be affected.");
	}
	
	/* (non-Javadoc)
	 * @see se.cqst.sleeper.parsers.Parser#printUsage()
	 * 
	 * Override default printUsage() and print usage information about PlainTextParser
	 */
	@Override
	public void printUsage()
	{
		print("PlainTextParser will be used to parse incoming data and compare it to the keyphrase. Data will be parsed as plain text");
		if(Boolean.valueOf(this.arguments.get("ptp_ci")))
			print("ptp_ci has been set and data will be compared case insensitive. Note that Providers will still treat the keyphrase"
					+ " case sensitive (unless specified otherwise), so care should be taken mixing the two");
	}

}
