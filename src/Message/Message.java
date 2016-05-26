package Message;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import Utils.Utils;

public class Message {
	
	public byte[] message;
	public String[] headercontent = null;
	public int lenght = 0;
	public byte[] body = null;
	public String header;
	/*
	public String MessageType = null;
	public String Version = null;
	public String SenderId = null;
	public String FileId = null;
	public String ChunkNo = null;
	*/

	public Message(byte[] message, int length) {
		
		this.message = message;
		this.lenght = length;
		
	}
	
	
	public boolean extractHeader() {
		
		if(message == null)
			return false;
		ByteArrayInputStream stream = new ByteArrayInputStream(message);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		

		
		try {
			header = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		headercontent = header.split("[ ]+");

		return true;
	}
	
	public boolean extractBody() {
		
		ByteArrayInputStream stream = new ByteArrayInputStream(message);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		String line = null;
		int headerLinesLengthSum = 0;
		int numLines = 0;
		

		do { //make sure body starts as soon as we find an empty line
			try {
				line = reader.readLine();

				headerLinesLengthSum += line.length();

				numLines++;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} while (!line.isEmpty());
		

		int bodyStartIndex = headerLinesLengthSum + numLines * Utils.CRLF.getBytes().length;

		body = Arrays.copyOfRange(message, bodyStartIndex,
				lenght);
		
		return true;
	}
	
	
	public boolean messageCheck() {
		if(headercontent.length < 3)
			return false;
		return true;
	}
	
	
	
	
	
	

}
