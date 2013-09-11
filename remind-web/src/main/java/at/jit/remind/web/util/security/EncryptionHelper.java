package at.jit.remind.web.util.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class EncryptionHelper
{
	private static final String digestAlgorithm = "SHA-512";
	private static final String stringEncoding = "UTF-8";

	private EncryptionHelper()
	{
	}

	public static byte[] digest(String value, byte[] salt, int iterations) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		MessageDigest digest = MessageDigest.getInstance(digestAlgorithm);
		digest.reset();
		digest.update(salt);

		byte[] byteValue = digest.digest(value.getBytes(stringEncoding));
		for (int i = 0; i < iterations; ++i)
		{
			digest.reset();
			byteValue = digest.digest(byteValue);
		}

		return byteValue;
	}
}
