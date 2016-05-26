package Server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import Encryption.Encryption;
import Utils.Utils;

public class Server {
	private static SecretKey key;
	public static volatile ServerDB serverDB;
	
	public static String mcAddress = null;
	public static String mcPort = null;

	public static String mdbAddress = null;
	public static String mdbPort = null;

	public static String mdrAddress = null;
	public static String mdrPort = null;


	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, ClassNotFoundException {
		
		/*
		 * ARGS
		 * 0 - server port
		 * 1 - MC Address
		 * 2 - MC Port
		 * 3 - MDB Address
		 * 4 - MDB Port
		 * 5 - MDR Address
		 * 6 - MDR Port
		 */
		
		if(args.length != 7) {
			System.out.println("Error: Invalid Arguments");
			System.out.println("USAGE: java server <server port> <MC Address> <MC Port> <MDB Address> <MDB Port> <MDR Address> <MDR Port>");
			System.exit(0);
		}
		
		//TO CHANGE //////////////////
		
		
		int port = Integer.parseInt(args[0]);
		
		mcAddress = args[1];
		mcPort = args[2];
				
		mdbAddress = args[3];
		mdbPort = args[4];
		
		mdrAddress = args[5];
		mdrPort = args[6];
		
		
		
		
		key = Encryption.generateSymmetricKey();
		
		System.out.println("Generated key: " + key.toString());

		loadDatabase();
		

		String str;

		ServerSocket srvSocket = new ServerSocket(port);

		System.out.println("Server initialized");
		while(true){
			
			System.out.println("Waiting for connections...");
			
			Socket socket = srvSocket.accept();

			PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
			InputStreamReader tempIn = new InputStreamReader(socket.getInputStream());
			BufferedReader in = new BufferedReader(tempIn);


			str = new String(in.readLine());

			str = processa(str);
			out.println(str);

			if(socket.isOutputShutdown()) {
				System.out.println("Closing!!!");
				socket.close();
			}
			
		}
		
	}

	private static String processa(String str) {

		str=str.trim();

		System.out.println("RECEIVED:" + str);

		String returnstr = "";

		String[] str_v = str.split(" ");
		if(str_v.length == 3) {
			if(str_v[0].equals("LOGIN")) {
				if(serverDB.checkPeer(str_v[1], str_v[2])) {
					returnstr = "SUCESS " + Encryption.getSymmetricKeyString(key) + " "
							+ mcAddress + " "
							+ mcPort + " "
							+ mdbAddress + " "
							+ mdbPort + " "
							+ mdrAddress + " "
							+ mdrPort;

				}
				
				else
					returnstr = "ERROR INVALID_USER_PASS";
			}
			else
				returnstr = "ERROR INVALID_MESSAGE_FORMAT";
		}
		else
			returnstr = "ERROR INVALID_MESSAGE_FORMAT";
		
		
		System.out.println("SENDING:" + returnstr);
		

		return returnstr;
	}
	
	//SERVER DATABASE
	private static void createDatabase() {
		serverDB = new ServerDB();
		saveDatabase();
	}

	private static void loadDatabase() throws ClassNotFoundException,IOException {
		Path serverPath = Paths.get("./server.data");
		try {
			FileInputStream fileInputStream = new FileInputStream(serverPath.toString());

			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

			serverDB = (ServerDB) objectInputStream.readObject();
			objectInputStream.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("Database not found. Creating...");
			createDatabase();
		}
	}

	public static void saveDatabase() {
		Path serverPath = Paths.get("./server.data");
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(serverPath.toString());

			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

			objectOutputStream.writeObject(serverDB);
			objectOutputStream.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("Database not found. Creating...");
			createDatabase();
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
