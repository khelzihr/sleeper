package se.cqst.sleeper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import se.cqst.sleeper.providers.Provider;

/**
 * <p>The <strong>SleeperTask</strong> class uses a {@link Provider} to check if a <i>key phrase</i> has been detected.
 * Upon detection, the specified task will execute and the <code>SleeperTask</code> will terminate.</p>
 * 
 * <p>The <code>SleeperTask</code> implements the <code>Singleton</code> pattern to simplify call-backs to the parent class</p>
 * 
 * @author Nicklas Rosvall Carlquist
 *
 */
public class SleeperTask  {
	
	public static final String	TASK_EXECUTE = "Keyphrase found, executing action...";
	public static final String TASK_EXPIRED = "Keyphrase has not been provided within time limit, exiting...";
	
	public static final String VERBOSE_NOTF = "Keyphrase was not found.";
	public static final String VERBOSE_COMP = "Execution of action complete.";
	
	private static SleeperTask instance = null;
	
	private Provider 	provider;
	private String		action;
	
	private int			repeat;
	
	private Timer timer;
	
	private boolean verbose;

	/**
	 * Create a new SleeperTask object
	 */
	private SleeperTask()
	{
		timer = new Timer();
		provider = null;
		action = "";
		verbose = false;
		setRepeat(5);
	}
	
	/**
	 * Returns the single SleeperTask instance.
	 * If the instance does not exist, create it.
	 * @return
	 */
	public static SleeperTask getInstance()
	{
		if(instance == null)
			instance = new SleeperTask();
		return instance;
	}
	
	public Provider getProvider()
	{
		return this.provider;
	}
	
	public void	setProvider(Provider provider)
	{
		this.provider = provider;
	}
	
	public String getAction()
	{
		return this.action;
	}
	
	public void setAction(String action)
	{
		this.action = action;
	}
	
	public boolean isVerbose()
	{
		return this.verbose;
	}
	
	public void setVerbose(boolean verbose)
	{
		this.verbose = verbose;
	}
	
	public int getRepeat() {
		return repeat;
	}

	public void setRepeat(int repeat) {
		if (repeat >= 3)
			this.repeat = repeat;
		if(verbose)
			print("Repeat set to: " + repeat + " min (min value allowed is 3 min)");
	}
	
	/**
	 * <p>Returns an instantiation of the TimerTask that will poll a {@link Provider} object. 
	 * If the <code>Provider</code> object returns true, execute the action specified by the provided
	 * arguments.</p>
	 * @return
	 */
	private TimerTask getTimerTask()
	{
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				if(SleeperTask.getInstance().getProvider().check())
				{
					print(SleeperTask.TASK_EXECUTE);
					StringBuffer out = new StringBuffer();
					Process p;
					
					try
					{
						p = Runtime.getRuntime().exec(SleeperTask.getInstance().getAction());
						p.waitFor();
						
						BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String line = "";
						while((line = reader.readLine()) != null)
						{
							out.append(line + '\n');
						}
					}
					catch(IllegalArgumentException ex)
					{
						print("No argument to execute was specified. Program will now terminate.");
						System.exit(0);
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
						System.exit(0);
					}
					
					System.out.println(out.toString());
					
					if(SleeperTask.getInstance().isVerbose())
						print(SleeperTask.VERBOSE_COMP);
					
					System.exit(0);
				}
				else
				{
					if(SleeperTask.getInstance().isVerbose())
						print(SleeperTask.VERBOSE_NOTF);
				}
			}
		};
		
		return task;
	}
	
	/**
	 * <p>Returns the current date and time in the yyyy-MM-dd HH:mm format</p>
	 * @return
	 */
	private String getDate()
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return sdf.format(cal.getTime());
	}
	
	/**
	 * <p>Prints to the System.out but with added prefix of {@link #getDate()}</p>
	 * @param text
	 */
	public void print(String text)
	{
		System.out.println("[" + getDate() + "] " + text);
	}
	
	/**
	 * <p>Executes the SleeperTask, scheduling the TimerTask object to execute at the interval specified
	 * by SleeperTask.repeat</p>
	 */
	public void run()
	{
		timer.scheduleAtFixedRate(this.getTimerTask(), 0, repeat * 60 * 1000);
	}

}
