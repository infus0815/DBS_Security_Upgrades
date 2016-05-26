package Initiators;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import Peer.Peer;
import Peer.Sender;

public class DeleteInitiator extends Thread{
	
	private String fileName;

	public DeleteInitiator(File file) {
		fileName = file.getName();
	}

	public void run() {
	
		if (Peer.database.fileHasBeenBackedUp(fileName)) {
		
			String fileID = Peer.database.getFileInfo(fileName).fileId;

			try {
				Sender.sendDELETE(fileID);
			} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
					| BadPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Peer.database.removeBackupFile(fileName);
		} else {
			System.out.println("no chunks found");
		}
	}

}
