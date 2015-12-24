package de.pepe4u.space.messenger;

/**
 * Batchjob to trigger sending of keep alive message to our neighbors
 * 
 * @license    GPL 2 (http://www.gnu.org/licenses/gpl.html)
 * @author Philipp Neuser <info@pepe-4u.de>
 *
 */
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
				Thread.sleep(KEEP_ALIVE_INTERVALL);
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
