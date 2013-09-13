package at.jit.remind.web.domain.context.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

import at.jit.remind.web.domain.base.model.EntityBase;
import at.jit.remind.web.domain.security.model.EncodedString;

@Entity
@Table(name = "PROPERTY")
@Access(AccessType.FIELD)
@AttributeOverrides({@AttributeOverride(name = "id", column = @Column(name = "PRO_ID")),
		@AttributeOverride(name = "version", column = @Column(name = "PRO_VERSION")),
		@AttributeOverride(name = "createdOn", column = @Column(name = "PRO_CREATED_ON", updatable = false)),
		@AttributeOverride(name = "modifiedOn", column = @Column(name = "PRO_MODIFIED_ON"))})
public class Property extends EntityBase
{
	private static final long serialVersionUID = 4466834230809265547L;

	@Column(name = "PRO_KEY")
	private String key;

	@Column(name = "PRO_VALUE")
	private String value;

	@Column(name = "PRO_ENCODED")
	private boolean encoded = false;

	@Embedded
	@AttributeOverrides({@AttributeOverride(name = "encoded", column = @Column(name = "PRO_ENCODED_VALUE")),
			@AttributeOverride(name = "salt", column = @Column(name = "PRO_SALT"))})
	private EncodedString encodedValue;

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getValue()
	{
		try
		{
			return isEncoded() ? (encodedValue != null ? encodedValue.decode() : "") : value;
		}
		catch (Exception e)
		{
			throw new PropertyEncodingException(e);
		}
	}

	public void setValue(String value)
	{
		if (isEncoded())
		{
			try
			{
				if (encodedValue == null)
				{
					encodedValue = new EncodedString();
				}

				encodedValue.encode(value);
			}
			catch (Exception e)
			{
				throw new PropertyEncodingException(e);
			}

			this.value = null;
		}
		else
		{
			encodedValue = null;
			this.value = value;
		}
	}

	public boolean isEncoded()
	{
		return encoded;
	}

	public void setEncoded(boolean encoded)
	{
		String currentValue = getValue();
		this.encoded = encoded;

		// store current value encoded if necessary
		setValue(currentValue);
	}

	public static final class PropertyEncodingException extends RuntimeException
	{
		private static final long serialVersionUID = 5815165216512819619L;

		public PropertyEncodingException(Exception e)
		{
			super(e);
		}
	}
}
