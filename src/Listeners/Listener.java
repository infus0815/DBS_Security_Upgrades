package Listeners;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import Encryption.Encryption;
import Message.Message;
import Peer.Peer;

public class Listener extends Thread {

	public MulticastSocket socket;

	public static final int PACKETMAXSIZE = 65000;


	public InetAddress address;
	public int port;

	public Listener(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}

	@Override
	public void run() {
		startSocket();

		byte[] buf = new byte[PACKETMAXSIZE];
		

		boolean done = false;
		while (!done) {
			try {
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				byte[] decMessage = Encryption.decryptMessage(packet.getData(),packet.getLength(),Peer.symmetricKey);
				Message message = new Message(decMessage,decMessage.length);

				if(!message.extractHeader() || !message.messageCheck())
					System.out.println("ERROR: Bad packet");
				else {
					// ignore packets sent by this peer
					if (!message.headercontent[2].equals(Peer.id)) {
						System.out.println("RECEIVED: " + message.header);
						ListHandler lhand = new ListHandler(message);
						lhand.start();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		closeSocket();
	}


	private void startSocket() {
		try {
			socket = new MulticastSocket(port);
			socket.setTimeToLive(1);
			socket.joinGroup(address);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void closeSocket() {
		if (socket != null)
			socket.close();
	}



}
