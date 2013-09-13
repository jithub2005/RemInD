package at.jit.remind.web.domain.security.model;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import at.jit.remind.web.util.security.EncryptionHelper;
import at.jit.remind.web.util.security.SecureRandomHelper;

@Embeddable
public class EncryptedString implements Serializable
{
	private static final long serialVersionUID = 2005364127636786247L;

	private static final int iterations = 1024;

	private String digest;
	private String salt;

	public String getDigest()
	{
		return digest;
	}

	public void setDigest(String digest)
	{
		this.digest = digest;
	}

	public String getSalt()
	{
		return salt;
	}

	public void setSalt(String salt)
	{
		this.salt = salt;
	}

	@Transient
	public void encrypt(String value) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		if (value == null)
		{
			setSalt(null);
			setDigest(null);

			return;
		}

		byte[] byteSalt = SecureRandomHelper.randomSalt();
		byte[] byteDigest = EncryptionHelper.digest(value, byteSalt, iterations);

		setSalt(Base64.encodeBase64String(byteSalt));
		setDigest(Base64.encodeBase64String(byteDigest));
	}

	@Transient
	public boolean validate(String value)
	{
		if (value == null || getSalt() == null)
		{
			return false;
		}

		byte[] byteDigest;
		try
		{
			byteDigest = EncryptionHelper.digest(value, Base64.decodeBase64(getSalt()), iterations);
		}
		catch (NoSuchAlgorithmException e)
		{
			return false;
		}
		catch (UnsupportedEncodingException e)
		{
			return false;
		}

		return StringUtils.equals(Base64.encodeBase64String(byteDigest), getDigest());
	}
}
