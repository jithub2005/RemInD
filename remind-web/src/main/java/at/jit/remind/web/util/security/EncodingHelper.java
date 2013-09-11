package at.jit.remind.web.util.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public final class EncodingHelper
{
	private static String algorithm = "AES";
	private static final String stringEncoding = "UTF-8";
	private static byte[] prefixBytes;

	static
	{
		try
		{
			prefixBytes = "Bd:k<:$&".getBytes("ISO-8859-1");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new EncodingHelperInitializationException(e);
		}
	}

	private EncodingHelper()
	{
	}

	public static String encode(String value, byte[] salt) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException
	{
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, createKey(salt));

		return String.valueOf(Hex.encodeHex(cipher.doFinal(value.getBytes(stringEncoding))));
	}

	public static String decode(String value, byte[] salt) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, DecoderException
	{
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, createKey(salt));

		return new String(cipher.doFinal(Hex.decodeHex(value.toCharArray())), stringEncoding);
	}

	private static Key createKey(byte[] salt)
	{
		byte[] keyBytes = new byte[prefixBytes.length + salt.length];
		System.arraycopy(prefixBytes, 0, keyBytes, 0, prefixBytes.length);
		System.arraycopy(salt, 0, keyBytes, prefixBytes.length, salt.length);

		return new SecretKeySpec(keyBytes, algorithm);
	}

	public static final class EncodingHelperInitializationException extends RuntimeException
	{
		private static final long serialVersionUID = 257157673998057477L;

		public EncodingHelperInitializationException(Exception e)
		{
			super(e);
		}
	}
}
