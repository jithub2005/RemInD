package at.jit.remind.web.domain.security.model;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;

import at.jit.remind.web.domain.base.model.EntityBase;

@Entity
@Table(name = "USERS")
@Access(AccessType.FIELD)
@AttributeOverrides({@AttributeOverride(name = "id", column = @Column(name = "USR_ID")),
		@AttributeOverride(name = "version", column = @Column(name = "USR_VERSION")),
		@AttributeOverride(name = "createdOn", column = @Column(name = "USR_CREATED_ON", updatable = false)),
		@AttributeOverride(name = "modifiedOn", column = @Column(name = "USR_MODIFIED_ON"))})
@NamedQueries({@NamedQuery(name = "User.findAll", query = "select u from User u"),
		@NamedQuery(name = "User.findByUsername", query = "select u from User u where u.username = :username"),
		@NamedQuery(name = "User.existsByUsername", query = "select count(u) from User u where u.username = :username"),
		@NamedQuery(name = "User.searchByUsername", query = "select u from User u where u.username like :username"),
		@NamedQuery(name = "User.setReadOnlyToFalseIfNull", query = "update User u set u.readOnly = false where u.readOnly is null")})
public class User extends EntityBase
{
	private static final long serialVersionUID = 2292469324438094157L;

	public static final String findAllQuery = User.class.getSimpleName() + ".findAll";
	public static final String findByUsernameQuery = User.class.getSimpleName() + ".findByUsername";
	public static final String existsByUsernameQuery = User.class.getSimpleName() + ".existsByUsername";
	public static final String searchByUsernameQuery = User.class.getSimpleName() + ".searchByUsername";
	public static final String setReadOnlyToFalseIfNull = User.class.getSimpleName() + ".setReadOnlyToFalseIfNull";

	public static final String usernameParameter = "username";

	@Size(min = 5, max = 20)
	@Column(name = "USR_USERNAME")
	private String username;

	@Size(min = 5, max = 20)
	@Transient
	private String password;

	@Size(max = 50)
	@Column(name = "USR_FIRST_NAME")
	private String firstName;

	@Size(max = 50)
	@Column(name = "USR_LAST_NAME")
	private String lastName;

	@Email(message = "Invalid mail address")
	@Column(name = "USR_MAIL_ADDRESS")
	private String mailAddress;

	@Column(name = "USR_VERIFIED")
	private boolean verified = false;

	@Column(name = "USR_READ_ONLY")
	private boolean readOnly = false;
	
	@Embedded
	@AttributeOverrides({@AttributeOverride(name = "digest", column = @Column(name = "USR_DIGEST")),
			@AttributeOverride(name = "salt", column = @Column(name = "USR_SALT"))})
	private EncryptedString credentials = new EncryptedString();

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	@Transient
	public String getPassword()
	{
		return credentials.getDigest();
	}

	@Transient
	public void setPassword(String password)
	{
		try
		{
			credentials.encrypt(password);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new PasswordEncryptionFailureException(e);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new PasswordEncryptionFailureException(e);
		}
	}

	@Transient
	public boolean validatePassword(String password)
	{
		return credentials.validate(password);
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getMailAddress()
	{
		return mailAddress;
	}

	public void setMailAddress(String mailAddress)
	{
		this.mailAddress = mailAddress;
	}

	public boolean isVerified()
	{
		return verified;
	}

	public void setVerified(boolean verified)
	{
		this.verified = verified;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public static final class PasswordEncryptionFailureException extends RuntimeException
	{
		private static final long serialVersionUID = -4255694886341401558L;

		public PasswordEncryptionFailureException(Exception e)
		{
			super(e);
		}
	}
}
