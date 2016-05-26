package Peer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import DB.Database;
import Filesystem.Filesystem;
import Initiators.BackupInitiator;
import Initiators.DeleteInitiator;
import Initiators.RestoreInitiator;
import Initiators.SpaceReclaimInitiator;
import Listeners.Listener;
import RMI.RMInterface;
import Encryption.Encryption;



public class Peer implements RMInterface {


	public static InetAddress mcAddress = null;
	public static int mcPort = 0;

	public static InetAddress mdbAddress = null;
	public static int mdbPort = 0;

	public static InetAddress mdrAddress = null;
	public static int mdrPort = 0;

	public static String id = null;
	public static int space = 0;
	public static MulticastSocket socket;
	public static SecretKey symmetricKey;
	
	public static PrivateKey privateKey;
	public static PublicKey publicKey;


	public static volatile Database database;

	public static void main(String[] args) throws IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
		// TODO Auto-generated method stub

		/*
		 * ARGS
		 * 0 - peer id
		 * 1 - MC Address
		 * 2 - MC Port
		 * 3 - MDB Address
		 * 4 - MDB Port
		 * 5 - MDR Address
		 * 6 - MDR Port
		 * 7 - Number of Chunks(Space available)
		 */


		//initiating stuff

		id = args[0];
		
		
		//public and private keys
		Encryption.generateRSAkeys();
		

		
		//SERVER STUFF
		String[] recMsgArr = new String[1];
		recMsgArr[0] = " ";
		
		

		
		while(!recMsgArr[0].equals("SUCESS")) {

			
			
			//<peerid> <pass> <serveraddr> <serverport> <Number of Chunks(Space available)>
			
			InputStreamReader tempIn2 = new InputStreamReader(System.in);
			BufferedReader in2 = new BufferedReader(tempIn2);
			
			System.out.println("Please insert your peer password:");
			String password = in2.readLine();
			

			InetAddress address = InetAddress.getByName(args[1]);
			int port = Integer.parseInt(args[2]);
			Socket socketServer = new Socket(address,port);

			PrintWriter out = new PrintWriter(socketServer.getOutputStream(),true);
			InputStreamReader tempIn = new InputStreamReader(socketServer.getInputStream());
			BufferedReader in = new BufferedReader(tempIn);

			String str = "LOGIN " + id + " " +  password;
			out.println(str);

			String recMsg = new String(in.readLine());

			recMsg=recMsg.trim();
			recMsgArr = recMsg.split(" ");




			if(recMsgArr[0].equals("SUCESS")) {
				
				System.out.println("LOGIN SUCESSFULL");

				symmetricKey = Encryption.getSymmetricKeyfromString(recMsgArr[1]);
				
				System.out.println("received key: " + symmetricKey.toString());


				mcAddress = InetAddress.getByName(recMsgArr[2]);
				mcPort = Integer.parseInt(recMsgArr[3]);

				//mdb
				mdbAddress = InetAddress.getByName(recMsgArr[4]);
				mdbPort = Integer.parseInt(recMsgArr[5]);

				//mdr
				mdrAddress = InetAddress.getByName(recMsgArr[6]);
				mdrPort = Integer.parseInt(recMsgArr[7]);

			}

			else if(recMsgArr[0].equals("ERROR"))
				System.out.println(recMsg);


			socketServer.shutdownOutput();
			socketServer.close();


		}
		



		space = Integer.parseInt(args[3]);
		socket = new MulticastSocket();

		//rmi
		Peer peer = new Peer();
		RMInterface stub = (RMInterface) UnicastRemoteObject.exportObject(peer, 0);
		// Bind the remote object's stub in the registry
		Registry registry = LocateRegistry.getRegistry();
		try {
			/*
			 * The name to which the remote object shall be bound (<remote_object_name>) 
			 * is passed to the server as a command line argument
			 */
			registry.bind(args[0], stub);				// args[0] = <remote_object_name>
		} catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Filesystem f = new Filesystem(args[0]);

		database = new Database();
		loadDatabase();


		//listeners
		Listener mcList = new Listener(mcAddress,mcPort);
		mcList.start();
		Listener mdbList = new Listener(mdbAddress,mdbPort);
		mdbList.start();
		Listener mdrList = new Listener(mdrAddress,mdrPort);
		mdrList.start();

		while(true);



	}

	private static void createDatabase() {
		database = new Database();
		saveDatabase();
	}

	private static void loadDatabase() throws ClassNotFoundException,IOException {
		try {
			FileInputStream fileInputStream = new FileInputStream(Filesystem.countfile.toString());

			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

			database = (Database) objectInputStream.readObject();
			objectInputStream.close();

		} catch (FileNotFoundException e) {
			System.out.println("Database not found. Creating...");
			createDatabase();
		}
	}

	public static void saveDatabase() {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(Filesystem.countfile.toString());

			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

			objectOutputStream.writeObject(database);
			objectOutputStream.close();

		} catch (FileNotFoundException e) {
			System.out.println("Database not found. Creating...");
			createDatabase();
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public String tarefa(String message) {
		String str;
		File file;

		String[] splitmessage = message.split(" ");

		switch(splitmessage[0]) {

		case "BACKUP":
			file = new File(splitmessage[1]);
			int repDeg = Integer.parseInt(splitmessage[2]);
			BackupInitiator bi = new BackupInitiator(file,repDeg);
			bi.start();

			break;
		case "RESTORE":
			file = new File(splitmessage[1]);
			RestoreInitiator ri = new RestoreInitiator(file);
			ri.start();
			break;
		case "DELETE":
			file = new File(splitmessage[1]);
			DeleteInitiator di = new DeleteInitiator(file);
			di.start();
			break;
		case "RECLAIM":
			int space = Integer.parseInt(splitmessage[1]);
			SpaceReclaimInitiator sri = new SpaceReclaimInitiator(space);
			sri.start();
			break;
		default:
			break;

		}

		System.out.println("RECEBEU: "+ message);
		str = processa(message);
		System.out.println("RESPOSTA: "+ str);

		return str;
	}


	String processa (String message){	// funcao eco
		return message;
	}




}
