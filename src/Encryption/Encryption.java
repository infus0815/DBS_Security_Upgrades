package Encryption;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import Peer.Peer;


public class Encryption {

	private static final String ALGORITHMSYMMETRIC = "AES/ECB/PKCS5PADDING"; 

	//Hashing passwords for server
	public static String hashPass(String password) {

		String sha1 = "";
		try
		{
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(password.getBytes("UTF-8"));
			sha1 = bytesToHex(crypt.digest());
		}
		catch(NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		return sha1;

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

	//Secret keys

	public static SecretKey generateSymmetricKey() throws NoSuchAlgorithmException {
		SecretKey key;

		KeyGenerator generator = KeyGenerator.getInstance("AES");
		generator.init(128);
		key  = generator.generateKey();


		return key;

	}

	public static String getSymmetricKeyString(SecretKey key) {

		String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
		return encodedKey;

	}

	public static SecretKey getSymmetricKeyfromString(String encodedKey) {

		byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
		SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
		return originalKey;
	}


	public static byte[] encryptMessage(byte[] message, SecretKey symkey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		byte[] encryptedmessage;




		Cipher cipher = Cipher.getInstance(ALGORITHMSYMMETRIC);
		cipher.init(Cipher.ENCRYPT_MODE, symkey);
		encryptedmessage = cipher.doFinal(message);


		return encryptedmessage;
	}

	public static byte[] decryptMessage(byte[] encryptedmessage, int messagesize, SecretKey symkey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {


		byte[] message;
		Cipher cipher = Cipher.getInstance(ALGORITHMSYMMETRIC);
		cipher.init(Cipher.DECRYPT_MODE, symkey);
		message = cipher.doFinal(encryptedmessage,0,messagesize);
		return message;
	}


	public static void generateRSAkeys() throws NoSuchAlgorithmException {

		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(1024);
		KeyPair kpar = keyGen.generateKeyPair();
		Peer.privateKey = kpar.getPrivate();
		Peer.publicKey = kpar.getPublic();
	}

	public static byte[] encryptKeyRSA(String skey, PublicKey pkey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
		cipher.init(Cipher.ENCRYPT_MODE, pkey);
		byte[] encryptedKey = cipher.doFinal(skey.getBytes());
		return encryptedKey;
	}


	public static SecretKey decryptKeyRSA(byte[] encryptedkey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {


		byte[] keyS;
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
		cipher.init(Cipher.DECRYPT_MODE, Peer.privateKey);
		keyS = cipher.doFinal(encryptedkey);
		SecretKey key = getSymmetricKeyfromString(new String(keyS));
		return key;

	}

	public static String getPublicKeyString() {
		
		String encodedKey = Base64.getEncoder().encodeToString(Peer.publicKey.getEncoded());
		return encodedKey;

		


	}
	
	public static PublicKey getPublicKeyfromString(String pkey) throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		byte[] publicBytes = Base64.getDecoder().decode(pkey);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey pubKey = keyFactory.generatePublic(keySpec);
		return pubKey;
		
	}
	
	public static String encodeByteBuffer(byte[] buf) {
		
		String bufString = Base64.getEncoder().encodeToString(buf);
		return bufString;
	}
	
	public static byte[] decodeByteBuffer(String bufString) {
		
		byte[] buf = Base64.getDecoder().decode(bufString);
		return buf;
	}



}
