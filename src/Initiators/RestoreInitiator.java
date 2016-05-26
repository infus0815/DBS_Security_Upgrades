package Initiators;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import Chunk.Chunk;
import DB.Fileinfo;
import Filesystem.Filesystem;
import Listeners.ListHandler;
import Peer.Peer;
import Peer.Sender;
import Utils.Utils;

public class RestoreInitiator extends Thread{

	private File file;

	public RestoreInitiator(File file) {
		this.file = file;
	}
	public void run() {
		if (Peer.database.fileHasBeenBackedUp(file.getName())) {
			Fileinfo fileinfo = Peer.database.getFileInfo(file.getName());
			ListHandler.chunksReceived.clear();

			for (int i = 0; i < fileinfo.nChunks; i++) {
				String chunkId = fileinfo.fileId + "_" + i;
				try {
					Sender.sendGETCHUNK(chunkId);
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
						| IllegalBlockSizeException | BadPaddingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			
			int timeout = 0;

			while(ListHandler.chunksReceived.size() < fileinfo.nChunks && timeout < 4) {
				try {
					Thread.sleep(500);
					System.out.println("Waiting for some chunks..");
					timeout++;
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			//restoring file
			byte[] fileData = new byte[0];
			

			for (int i = 0; i < fileinfo.nChunks; i++) {
				Chunk rightChunk = null;

				for (Chunk chunk : ListHandler.chunksReceived) {
					String[] ids = chunk.id.split("_");
					int chunkn = Integer.parseInt(ids[1]);
					if (chunkn == i && ids[0].equals(fileinfo.fileId)) {
						rightChunk = chunk;
						break;
					}
				}

				if (rightChunk == null) {
					System.out.println("Missing file chunk.");
					return;
				}
				

				try {
					fileData = Utils.joinArrays(fileData, rightChunk.data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			try {
				Filesystem.saveFile(file.getName(), fileData);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("The requested file can not be restored.");
		}
	}

}
