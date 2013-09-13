package at.jit.remind.web.domain.context.reporting.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import at.jit.remind.web.domain.base.model.EntityBase;

@Entity
@Table(name = "ACTION_LOG")
@AttributeOverrides({@AttributeOverride(name = "id", column = @Column(name = "ACL_ID")),
		@AttributeOverride(name = "version", column = @Column(name = "ACL_VERSION")),
		@AttributeOverride(name = "createdOn", column = @Column(name = "ACL_CREATED_ON", updatable = false)),
		@AttributeOverride(name = "modifiedOn", column = @Column(name = "ACL_MODIFIED_ON"))})
public class ActionLog extends EntityBase
{
	private static final long serialVersionUID = 3333663891562840271L;

	public ActionLog()
	{
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "A2A_ACT_ID")
	private Action action;

	@Column(name = "ACL_TEXT", length = 250)
	private String logText;

	@Column(name = "ACL_LEVEL")
	private String logLevel;

	public String getLogText()
	{
		return logText;
	}

	public Action getAction()
	{
		return action;
	}

	public void setAction(Action action)
	{
		this.action = (Action) action;
	}

	public void setLogText(String logText)
	{
		this.logText = logText;
	}

	public String getLogLevel()
	{
		return logLevel;
	}

	public void setLogLevel(String logLevel)
	{
		this.logLevel = logLevel;
	}

}
