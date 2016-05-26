package Utils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Utils {


	public static final String VERSION = "1.0";
	public static final String CRLF = "\r\n";
	
	public static Random random = new Random();

	public static String generateFID(String fileID, String serverId, String dateModified) {

		MessageDigest digest = null;

		try {
			digest = MessageDigest.getInstance("SHA-256");
		} 
		catch (NoSuchAlgorithmException e) {
			System.err.println(e.getMessage());
		}

		String s = fileID + serverId + dateModified;

		System.out.println(s);

		byte[] hash = digest.digest(s.getBytes(StandardCharsets.UTF_8));

		String result = bytesToHex(hash);
		System.out.println(result);

		return result;
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	private static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static byte[] joinArrays(byte a[],byte b[]) throws IOException {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write( a );
		outputStream.write( b );

		byte c[] = outputStream.toByteArray( );
		return c;

	}
}
