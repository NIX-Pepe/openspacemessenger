package de.pepe4u.space.messenger;

public class KeepAliveTrigger extends Thread {
	private static final int KEEP_ALIVE_INTERVALL = 5000;
	
	private MessageManager mm;
	private boolean keepRunning;
	
	public KeepAliveTrigger(MessageManager mm)
	{
		keepRunning = true;
		this.mm = mm;
	}
	
	@Override
	public void run()
	{
		while(keepRunning)
		{
			try{
				mm.sendKeepAliveToNeighborhood();
				this.sleep(KEEP_ALIVE_INTERVALL);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void shutdownThread()
	{
		keepRunning = false;
	}
}
