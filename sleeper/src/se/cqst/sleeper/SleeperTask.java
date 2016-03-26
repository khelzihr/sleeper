package se.cqst.sleeper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import se.cqst.sleeper.providers.Provider;

public class SleeperTask  {
	
	public static final String	TASK_EXECUTE = "Keyphrase found, executing action...";
	public static final String TASK_EXPIRED = "Keyphrase has not been provided within time limit, exiting...";
	
	public static final String VERBOSE_NOTF = "Keyphrase was not found.";
	public static final String VERBOSE_COMP = "Execution of action complete.";
	
	private static SleeperTask instance = null;
	
	private Provider 	provider;
	private String		action;
	
	private Timer timer;
	
	private boolean verbose;

	
	private SleeperTask()
	{
		timer = new Timer();
		provider = null;
		action = "";
		verbose = false;
	}
	
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
	
	private TimerTask getTimerTask()
	{
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				if(SleeperTask.getInstance().getProvider().check())
				{
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
	
	private String getDate()
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return sdf.format(cal.getTime());
	}
	
	public void print(String text)
	{
		System.out.println("[" + getDate() + "] " + text);
	}
	
	public void run()
	{
		timer.scheduleAtFixedRate(this.getTimerTask(), 0, 10 * 60 * 1000);
	}

}
