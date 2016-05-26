package Chunk;

public class Chunk {
	
	public static final int MAXSIZE = 64000;
	public String id;
	public int replicationDegree;
	public byte[] data;
	public String peerId;

	public Chunk(String fileId, int chunkNumber, int replicationDegree, byte[] data) {
		this.id = fileId + "_" + chunkNumber;

		this.replicationDegree = replicationDegree;

		this.data = data;
	}
	
	public Chunk(String chunkId, int replicationDegree, byte[] data) {
		this.id = chunkId;
		
		this.replicationDegree = replicationDegree;

		this.data = data;
	}

}
