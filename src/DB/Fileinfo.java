package DB;

import java.io.Serializable;

public class Fileinfo implements Serializable{
	
	private static final long serialVersionUID = 1L;

	public String fileId;
	public int nChunks;

	public Fileinfo(String fileId, int nChunks) {
		this.fileId = fileId;
		this.nChunks = nChunks;
	}


}
