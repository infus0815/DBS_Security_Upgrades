package Listeners;

import Message.Message;
import Message.MessageType;
import Peer.Peer;
import Peer.Sender;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import Chunk.Chunk;
import Encryption.Encryption;
import Filesystem.Filesystem;
import Utils.Utils;

public class ListHandler extends Thread {

	private final Message message;

	private static ArrayList<String> CHUNKSsent = new ArrayList<String>();
	private static ArrayList<String> PutchunksReceived = new ArrayList<String>();
	public static ArrayList<String> storedReceived = new ArrayList<String>();
	public static ArrayList<Chunk> chunksReceived = new ArrayList<Chunk>();

	public ListHandler(Message message) {
		this.message = message;
	}

	public void run() {

		MessageType messagetype = MessageType.valueOf(message.headercontent[0]);

		switch (messagetype) {

		case PUTCHUNK:
			handlePUTCHUNK();
			break;

		case STORED:
			handleSTORED();
			break;

		case GETCHUNK:
			try {
				try {
					handleGETCHUNK();
				} catch (InvalidKeySpecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;

		case CHUNK:
			try {
				handleCHUNK();
			} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
					| BadPaddingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;

		case DELETE:
			try {
				handleDELETE();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case REMOVED:
			try {
				handleREMOVED();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		default:
			break;
		}

	}


	private void handlePUTCHUNK() {
		if(PutchunksReceived == null)
			PutchunksReceived = new ArrayList<String>();

		if (Peer.database.canSaveChunksOf(message.headercontent[3])) {

			String chunkId = message.headercontent[3] + "_" + message.headercontent[4];

			int replicationDeg = Integer.parseInt(message.headercontent[5]);

			PutchunksReceived.add(chunkId);

			message.extractBody();

			if (Filesystem.numberOfFiles < Peer.space) {
				try {
					Thread.sleep(Utils.random.nextInt(400));
					if (Filesystem.fileExists(chunkId.toString())) {
						Sender.sendSTORED(chunkId);
					}
					else {
						Sender.sendSTORED(chunkId);
						Filesystem.saveChunk(chunkId, replicationDeg, message.body);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}


	}

	private void handleSTORED() {

		String chunkId = message.headercontent[3] + "_" + message.headercontent[4];
		String senderId = message.headercontent[2];

		Peer.database.addChunkMirror(chunkId, senderId);
		storedReceived.add(chunkId);

	}


	private void handleGETCHUNK() throws IOException, InvalidKeySpecException {
		String chunkId = message.headercontent[3] + "_" + message.headercontent[4];
		CHUNKSsent.clear();

		if (Peer.database.containsChunk(chunkId)) {
			try {
				Thread.sleep(Utils.random.nextInt(400));
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}

			boolean chunkAlreadySent = CHUNKSsent.contains(chunkId);

			if (!chunkAlreadySent) {
				try {
					byte[] data = Filesystem.loadChunk(chunkId);
					
					//generate and encrypt data with symkey + symkey with rsa
					SecretKey skey = Encryption.generateSymmetricKey();
					byte[] encdata = Encryption.encryptMessage(data, skey);
					
					
					String skeyString = Encryption.getSymmetricKeyString(skey);
					byte[] enckey = Encryption.encryptKeyRSA(skeyString, Encryption.getPublicKeyfromString(message.headercontent[5]));
					String enckeyString = Encryption.encodeByteBuffer(enckey);
					
					Chunk chunk = new Chunk(chunkId, 1, encdata);

					Sender.sendCHUNK(chunk,enckeyString);

				} catch (FileNotFoundException e) {
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
			CHUNKSsent.remove(chunkId);
		}

	}

	private void handleCHUNK() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String chunkId = message.headercontent[3] + "_" + message.headercontent[4];
		CHUNKSsent.add(chunkId);

		message.extractBody();
		
		//decrypt symkey with private rsakey + decrypt data with symkey
		byte[] encryptedskey = Encryption.decodeByteBuffer(message.headercontent[5]);
		SecretKey skey = Encryption.decryptKeyRSA(encryptedskey);
		byte[] realdata = Encryption.decryptMessage(message.body, message.body.length, skey);

		Chunk chunk = new Chunk(chunkId,1,realdata);
		chunksReceived.add(chunk);

	}


	private void handleDELETE() throws IOException {
		String fileId = message.headercontent[3];

		ArrayList<String> chunksToBeDeleted = Peer.database.getChunkIDsOfFile(fileId);

		while (!chunksToBeDeleted.isEmpty()) {
			String chunkID = chunksToBeDeleted.remove(0);

			Filesystem.deleteChunk(chunkID);
		}

	}

	private void handleREMOVED() throws IOException {
		//TODO
		String chunkId = message.headercontent[3] + "_" + message.headercontent[4];
		String peerId = message.headercontent[2];

		PutchunksReceived.clear();

		if (Peer.database.containsChunk(chunkId)) {
			// updating available mirrors of chunk
			Peer.database.removeChunkMirror(chunkId, peerId);

			int currentRep = Peer.database.getChunkMirrorsSize(chunkId);
			int desiredRep = Peer.database.getChunkReplicationDegree(chunkId);

			if (currentRep < desiredRep) {
				try {
					Thread.sleep(Utils.random.nextInt(400));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}


				boolean state = PutchunksReceived.contains(chunkId);


				if (!state) {
					try {
						byte[] data = Filesystem.loadChunk(chunkId);

						Chunk chunk = new Chunk(chunkId,desiredRep,data);
						Sender.sendPUTCHUNK(chunk);

					} catch (FileNotFoundException e) {
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
			}
		}

	}





}
