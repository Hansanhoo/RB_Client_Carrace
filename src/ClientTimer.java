import java.util.concurrent.TimeUnit;

/**
 * timer for client
 * @author hansanhoo
 *
 */
public class ClientTimer implements Runnable
{
	private int duration;
	
	//client this timer is running on
	private Client client;

	/**
	 * Timer for Client
	 * 
	 * @param duration
	 * @param client
	 */
	public ClientTimer(int duration, Client client)
	{
		this.duration = duration;
		this.client = client;
	}

	@Override
	public void run()
	{
		this.startTimer();
	}
	/**
	 * starts a Timer
	 */
	public void startTimer()
	{
		while (this.duration > 0)
		{
			try
			{
				this.duration--;
				TimeUnit.SECONDS.sleep(1);
			}
			catch (InterruptedException e)
			{
				System.out.println(e.getMessage());
			}
		}

		this.client.setTimerActive(false);

	}

	public int getDuration()
	{
		return this.duration;
	}
}
