import java.util.concurrent.ThreadPoolExecutor;

/**
 * starts a client
 * 
 * @author hansanhoo
 *
 */
public class startUpClient
{
	public static void main(String[] args)
	{
		Client client1 = new Client(5555);
		Thread clientTh = new Thread(client1);
		clientTh.start();
	}
}
