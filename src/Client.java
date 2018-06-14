import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Represents a Client
 * 
 * @author hansanhoo
 *
 */
public class Client implements Runnable
{
	private InetAddress host;
	private int port;
	private boolean timerActive;

	// List of Cars
	private List<String> cars;

	// Socket to server
	private Socket s1 = null;

	// inputStream
	private BufferedReader is = null;
	// outputStream
	private BufferedWriter os = null;
	// Timer
	private ClientTimer timer;
	// sendHelper
	private SendToServer sendToServer;

	/**
	 * Konstruktor with host and port as param
	 * 
	 * @param host
	 * @param port
	 */
	public Client(InetAddress host, int port)
	{
		this.host = host;
		this.port = port;
		this.cars = new ArrayList<>();
	}

	/**
	 * Konstruktor with port as param gets host as localhost
	 * 
	 * @param host
	 * @param port
	 */
	public Client(int port)
	{
		try
		{
			this.host = InetAddress.getLocalHost();
			cars = new ArrayList<>();
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		this.port = port;
	}
	/**
	 * start of Client
	 */
	@Override
	public void run()
	{
		// Open Socket Input- and Outputstream
		try
		{
			s1 = new Socket(this.host, this.port);
			is = new BufferedReader(new InputStreamReader(s1.getInputStream()));
			os = new BufferedWriter(
					new OutputStreamWriter(s1.getOutputStream()));

		}
		catch(SocketException se) {
			se.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.err.print("IO Exception");
		}
		

		System.out.println("Client Address : " + this.host);
		//listen For Server Input and open Thread for Output to Server
		listenForServer();
		sendToServer();
	}
	
	/**
	 * showsCountdown if timer is active
	 * status of cars if timer is not active
	 */
	private void showInfo()
	{
		System.out.println("Info:");
		if (this.timerActive)
		{
			showCountDown();
		}
		else
		{
			showStatusOfCars();
		}

	}

	/**
	 * Shows registered Cars
	 */
	public void showStatusOfCars()
	{
		System.out.println("Status \t| Name");
		for (String c : cars)
		{
			System.out.println("Registered \t| Name : " + c);
		}
	}

	/**
	 * clears the LineUp
	 */
	public void clearCars()
	{
		this.cars = new ArrayList<>();
	}

	/**
	 * takes the oldLineUp again
	 */
	public void takeOldLineUp()
	{
		ArrayList<String> helper = new ArrayList<>();
		for (String c : cars)
		{
			helper.add(c);
		}
		cars = new ArrayList<>();
		for (String c : helper)
		{
			sendToServer.registerCar(c);
		}

	}

	/**
	 * sets Status of Timer
	 * 
	 * @param status
	 */
	public void setTimerActive(Boolean status)
	{
		this.timerActive = status;
	}

	/**
	 * showsCountDown of Timer
	 */
	public void showCountDown()
	{
		if (timer != null)
		{
			System.out.println("Remaining:" + timer.getDuration() + " seconds till the race will start! Register now");
		}
	}

	/**
	 * starts Timer thread
	 * 
	 * @param time
	 */
	public void startCountDown(int time)
	{
		setTimerActive(true);
		timer = new ClientTimer(time, this);
		Thread clientTimerThread = new Thread(timer);
		clientTimerThread.start();
	}

	/**
	 * start Thread to Listen for Input From Server
	 */
	private void listenForServer()
	{
		ListenerForServer listernForServer = new ListenerForServer();
		Thread listenerForServerThread = new Thread(listernForServer);
		listenerForServerThread.start();
	}

	/**
	 * start Thread to send Input To Server
	 */
	private void sendToServer()
	{
		sendToServer = new SendToServer();		
		sendToServer.startUpSend();
	}

	

	/**
	 * Scans user client input and sends it to server
	 * 
	 * @author hansanhoo
	 *
	 */
	// TODO: runnable rausnehmen! geht auch ohne!!!
	private class SendToServer
	{		
		
		String askForCar = "Bitte geben Sie ihren Namen ein ( Enter quit to end \n"
				+ " info to get info \n new to to register a new Line Up \n old to take the old Line Up):";

		public void startUpSend()
		{
			askForCar = "Bitte geben Sie ihren Namen ein ( Enter quit to end \n"
					+ " info to get info \n new to to register a new Line Up \n old to take the old Line Up):";

			System.out.println(askForCar);
			// schreiben - > Server namen entgegen nehmen und Registrieren dann
			// duration zurück zum Client Timer starten!
			
			Scanner sc = new Scanner(System.in);
			String line = sc.nextLine();

			while (line.compareTo("quit") != 0)
			{
				switch (line)
				{
					case "info":
						showInfo();
						break;
					case "new":
						clearCars();
						System.out.println(askForCar);
						break;
					case "old":
						takeOldLineUp();
						break;
					default:
						registerCar(line);
						break;
				}
				line = sc.nextLine();

			}
			sc.close();
		}

		/**
		 * takes a name and sends it to server
		 * 
		 * @param name
		 */
		public void registerCar(String name)
		{
			if (!cars.contains(name))
			{
				cars.add(name);
				try
				{
					os.write(name);
					os.newLine();
					os.flush();
					showCountDown();
					System.out.println(askForCar);
				}
				catch(SocketException se) {
					se.printStackTrace();
				}
				catch (IOException e)
				{					
					e.printStackTrace();
				}

			}
			else
			{
				String otherName = "Bitte geben Sie einen anderen Auto Namen ein; Ihr eingegebener Name ist schon vorhanden!!\n";
				askForCar += otherName;
				System.out.println(askForCar);

				showCountDown();

			}
		}

	}

	/**
	 * Listens for Server input
	 * 
	 * @author hansanhoo
	 *
	 */
	private class ListenerForServer implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				String s = is.readLine();
				while (s != null)
				{
					if (s.matches("\\d+"))
					{
						if (!timerActive)
						{
							String countDown = s;
							startCountDown(Integer.valueOf(countDown));
							showCountDown();
						}
					}
					else
					{
						System.out.println(s);
					}
					s = is.readLine();

				}
			}
			catch(SocketException se) {
				se.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		}
	}
}
