package at.jit.remind.web.util.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class SecureRandomHelper
{
	private static final String randomNumberGeneratorAlgorithm = "SHA1PRNG";
	private static final int saltArraySize = 8;

	private SecureRandomHelper()
	{
	}

	public static byte[] randomSalt() throws NoSuchAlgorithmException
	{
		SecureRandom random = SecureRandom.getInstance(randomNumberGeneratorAlgorithm);
		byte[] byteSalt = new byte[saltArraySize];
		random.nextBytes(byteSalt);

		return byteSalt;
	}
}
