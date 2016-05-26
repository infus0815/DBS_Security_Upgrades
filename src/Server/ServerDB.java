package Server;

import java.io.Serializable;
import java.util.HashMap;

import Encryption.Encryption;

public class ServerDB implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private volatile HashMap<String, String> peerInfo;

	public ServerDB() {
		
		this.peerInfo = new HashMap<String, String>();
		
	}
	
	@Override
	public String toString() {
		return "ServerDB [peerInfo=" + peerInfo + "]";
	}

	private synchronized void addPeer(String peerId, String password) {
		
		peerInfo.put(peerId, password);
		Server.saveDatabase();
		
	}
	
	public synchronized boolean checkPeer(String peerId, String password) {
		
		
		String hashedPass = Encryption.hashPass(password);
		
		
				
		if(peerInfo.containsKey(peerId)) {
			if(!peerInfo.get(peerId).equals(hashedPass))
				return false;
		}
		
		else {
			System.out.println("Peer " + peerId + " does not exist. Creating...");
			addPeer(peerId,hashedPass);
		}
		
		return true;
		
	}
	
	
	
	
	

}
