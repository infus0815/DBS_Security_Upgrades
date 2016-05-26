package Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import Chunk.Chunk;
import Encryption.Encryption;
import Message.MessageType;
import Utils.Utils;

public class Sender extends Thread {
	
	private synchronized static void sendpacketMC(byte[] buffer) {
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, Peer.mcAddress, Peer.mcPort);
		try {
			Peer.socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private synchronized static void sendpacketMDB(byte[] buffer) {
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, Peer.mdbAddress, Peer.mdbPort);
		try {
			Peer.socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private synchronized static void sendpacketMDR(byte[] buffer) {
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, Peer.mdrAddress, Peer.mdrPort);
		try {
			Peer.socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void sendPUTCHUNK(Chunk chunk) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String header = MessageType.PUTCHUNK + " " + Utils.VERSION;
		
		String[] chunkidsplit = chunk.id.split("_");
		header += " " + Peer.id;
		header += " " + chunkidsplit[0];
		header += " " + chunkidsplit[1];
		header += " " + chunk.replicationDegree;
		header += " " + Utils.CRLF;
		header += Utils.CRLF;
		
		System.out.println("SENDING: PUTCHUNK --> Fileid: " + chunkidsplit[0] + " Chunkid: " + chunkidsplit[1]);
		
		byte[] toSend = Utils.joinArrays(header.getBytes(), chunk.data);
		byte[] toSendEnc = Encryption.encryptMessage(toSend, Peer.symmetricKey);
		
		//System.out.println("SENDING DATA: " + new String(chunk.data)); //TO TEST

		sendpacketMDB(toSendEnc);
	}

	public static void sendSTORED(String chunkId) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String header = MessageType.STORED + " " + Utils.VERSION;
		header += " " + Peer.id;
		String[] chunkidsplit = chunkId.split("_");
		header += " " + chunkidsplit[0];
		header += " " + chunkidsplit[1];
		header += " " + Utils.CRLF;
		header += Utils.CRLF;
		
		System.out.println("SENDING: STORED --> Fileid: " + chunkidsplit[0] + " Chunkid: " + chunkidsplit[1]);
		
		byte[] toSendEnc = Encryption.encryptMessage(header.getBytes(), Peer.symmetricKey);

		sendpacketMC(toSendEnc);
	}

	public static void sendGETCHUNK(String chunkId) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String header = MessageType.GETCHUNK + " " + Utils.VERSION;
		header += " " + Peer.id;
		String[] chunkidsplit = chunkId.split("_");
		header += " " + chunkidsplit[0];
		header += " " + chunkidsplit[1];
		header += " " + Encryption.getPublicKeyString();
		header += " " + Utils.CRLF;
		header += Utils.CRLF;
		
		System.out.println("SENDING: GETCHUNK --> Fileid: " + chunkidsplit[0] + " Chunkid: " + chunkidsplit[1]);
		
		byte[] toSendEnc = Encryption.encryptMessage(header.getBytes(), Peer.symmetricKey);

		sendpacketMC(toSendEnc);
	}
	
	
	
	//USED IN RESTORES
	public static void sendCHUNK(Chunk chunk, String skey) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String header = MessageType.CHUNK + " " + Utils.VERSION;
		header += " " + Peer.id;
		String[] chunkidsplit = chunk.id.split("_");
		header += " " + chunkidsplit[0];
		header += " " + chunkidsplit[1];
		header += " " + skey;
		header += " " + Utils.CRLF;
		header += Utils.CRLF;
		
		System.out.println("SENDING: CHUNK --> Fileid: " + chunkidsplit[0] + " Chunkid: " + chunkidsplit[1]);
		
		byte[] toSend = Utils.joinArrays(header.getBytes(), chunk.data);
		byte[] toSendEnc = Encryption.encryptMessage(toSend, Peer.symmetricKey);
		
		
		//System.out.println("SENDING DATA: " + new String(chunk.data)); //TO TEST
		
		sendpacketMDR(toSendEnc);
	}

	public static void sendDELETE(String fileId) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String header = MessageType.DELETE + " " + Utils.VERSION;
		header += " " + Peer.id;
		header += " " + fileId;
		header += " " + Utils.CRLF;
		header += Utils.CRLF;
		
		System.out.println("SENDING: DELETE --> Fileid: " + fileId);
		
		byte[] toSendEnc = Encryption.encryptMessage(header.getBytes(), Peer.symmetricKey);

		sendpacketMC(toSendEnc);
	}

	public static void sendREMOVED(String chunkId) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String header = MessageType.REMOVED + " " + Utils.VERSION;
		header += " " + Peer.id;
		String[] chunkidsplit = chunkId.split("_");
		header += " " + chunkidsplit[0];
		header += " " + chunkidsplit[1];
		header += " " + Utils.CRLF;
		header += Utils.CRLF;
		
		System.out.println("SENDING: REMOVE --> Fileid: " + chunkidsplit[0] + " Chunkid: " + chunkidsplit[1]);
		
		byte[] toSendEnc = Encryption.encryptMessage(header.getBytes(), Peer.symmetricKey);

		sendpacketMC(toSendEnc);
	}


}
