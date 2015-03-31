package fmg.common.crypt;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Simple3DES {

	private byte[] keyData;

	public Simple3DES(String key) {
		if (key == null)
			throw new IllegalArgumentException("Need key value");
		if (key.length() < 8)
			throw new IllegalArgumentException("Small key length");
		try {
			this.keyData = key.getBytes("UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	public byte[] encrypt(byte[] plainData) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		MessageDigest md = MessageDigest.getInstance("md5");
		byte[] digestOfPassword = md.digest(keyData);
		byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
		for (int j = 0, k = 16; j < 8; )
			keyBytes[k++] = keyBytes[j++];

		SecretKey skey = new SecretKeySpec(keyBytes, "DESede");
		IvParameterSpec iv = new IvParameterSpec(new byte[8]);
		Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, skey, iv);

		byte[] cipherText = cipher.doFinal(plainData);
		return cipherText;
	}

	public byte[] decrypt(byte[] cryptData) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException  {
		MessageDigest md = MessageDigest.getInstance("md5");
		byte[] digestOfPassword = md.digest(keyData);
		byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
		for (int j = 0, k = 16; j < 8; )
			keyBytes[k++] = keyBytes[j++];

		SecretKey skey = new SecretKeySpec(keyBytes, "DESede");
		IvParameterSpec iv = new IvParameterSpec(new byte[8]);
		Cipher decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		decipher.init(Cipher.DECRYPT_MODE, skey, iv);

		byte[] plainData = decipher.doFinal(cryptData);
		return plainData;
	}
}
