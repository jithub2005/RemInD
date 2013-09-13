package at.jit.remind.web.domain.security.model;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;

import at.jit.remind.web.util.security.EncodingHelper;
import at.jit.remind.web.util.security.SecureRandomHelper;

@Embeddable
public class EncodedString
{
	private String encoded;
	private String salt;

	public String getEncoded()
	{
		return encoded;
	}

	public void setEncoded(String encoded)
	{
		this.encoded = encoded;
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
	public void encode(String value) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, UnsupportedEncodingException,
			IllegalBlockSizeException, BadPaddingException
	{
		if (value == null)
		{
			reset();

			return;
		}

		byte[] byteSalt = SecureRandomHelper.randomSalt();

		setSalt(Base64.encodeBase64String(byteSalt));
		setEncoded(EncodingHelper.encode(value, byteSalt));
	}

	@Transient
	public String decode() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
			UnsupportedEncodingException, DecoderException
	{
		if (getEncoded() == null || getSalt() == null)
		{
			return null;
		}

		return EncodingHelper.decode(getEncoded(), Base64.decodeBase64(getSalt()));
	}

	@Transient
	public void reset()
	{
		setSalt(null);
		setEncoded(null);
	}
}
