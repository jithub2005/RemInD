package at.jit.remind.web.domain.context.reporting.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import at.jit.remind.web.domain.base.model.EntityBase;
import at.jit.remind.web.domain.security.model.User;

@Entity
@Table(name = "FILE_INFO")
@AttributeOverrides({@AttributeOverride(name = "id", column = @Column(name = "FIN_ID")),
		@AttributeOverride(name = "version", column = @Column(name = "FIN_VERSION")),
		@AttributeOverride(name = "createdOn", column = @Column(name = "FIN_CREATED_ON", updatable = false)),
		@AttributeOverride(name = "modifiedOn", column = @Column(name = "FIN_MODIFIED_ON"))})
public class FileInfo extends EntityBase
{
	private static final long serialVersionUID = 8324772269267103442L;

	public FileInfo()
	{
	}

	@Column(name = "FIN_NAME")
	private String name;

	@Lob
	@Column(name = "FIN_CONTENT")
	private String content;

	@Column(name = "FIN_MD5")
	private String md5;

	@Column(name = "FIN_SESSION_ID")
	private String sessionId;

	@ManyToOne
	@JoinColumn(name = "F2U_USR_ID")
	private User user;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Transient
	public String getContent()
	{
		return new String(Base64.decodeBase64(content.getBytes()));
	}

	@Transient
	public void setContent(String content)
	{
		this.content = Base64.encodeBase64String(content.getBytes());
		this.md5 = DigestUtils.md5Hex(content);
	}

	public String getMd5()
	{
		return md5;
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public void setSessionId(String sessionId)
	{
		this.sessionId = sessionId;
	}

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
	{
		this.user = user;
	}
}
