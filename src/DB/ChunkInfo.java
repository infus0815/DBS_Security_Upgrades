package DB;

import java.io.Serializable;
import java.util.ArrayList;

public class ChunkInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public String chunkId;
	public ArrayList<String> mirrors;
	public int replicationDegree;
	
	
	public ChunkInfo(int replicationDegree, ArrayList<String> mirrors) {
		this.replicationDegree = replicationDegree;
		this.mirrors = mirrors;
	}
	
	public void removeMirror(String mirror) {
		mirrors.remove(mirror);
	}

}
