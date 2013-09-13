package at.jit.remind.web.domain.context.reporting.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import at.jit.remind.core.context.messaging.ActionType;
import at.jit.remind.web.domain.base.model.EntityBase;
import at.jit.remind.web.domain.security.model.User;

@Entity
@Table(name = "ACTION")
@AttributeOverrides({@AttributeOverride(name = "id", column = @Column(name = "ACT_ID")),
		@AttributeOverride(name = "version", column = @Column(name = "ACT_VERSION")),
		@AttributeOverride(name = "createdOn", column = @Column(name = "ACT_CREATED_ON", updatable = false)),
		@AttributeOverride(name = "modifiedOn", column = @Column(name = "ACT_MODIFIED_ON"))})
public class Action extends EntityBase
{
	private static final long serialVersionUID = -5689399427310201654L;

	public Action()
	{
	}

	@Column(name = "ACT_SESSION_ID")
	private String sessionId;

	@ManyToOne
	@JoinColumn(name = "A2U_USR_ID")
	private User user;

	@Column(name = "ACT_TYPE")
	private String type;

	@Column(name = "ACT_ENDED")
	private Date ended;

	@ManyToOne()
	@JoinColumn(name = "A2F_FIN_ID", unique = false, nullable = true, updatable = false)
	private FileInfo fileInfo;

	@ManyToOne
	@JoinColumn(name = "A2A_ACT_ID")
	private Action parentAction;

	@OneToMany(mappedBy = "parentAction")
	@OrderBy("id")
	private List<Action> childActions = new ArrayList<Action>();

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

	public String getType()
	{
		return type;
	}

	@Transient
	public void setType(ActionType actionType)
	{
		this.type = actionType.name();
	}

	public Date getEnded()
	{
		return ended;
	}

	public void setEnded(Date ended)
	{
		this.ended = ended;
	}

	public FileInfo getFileInfo()
	{
		return fileInfo;
	}

	public void setFileInfo(FileInfo fileInfo)
	{
		this.fileInfo = fileInfo;
	}

	public Action getParentAction()
	{
		return parentAction;
	}

	public void setParentAction(Action parentAction)
	{
		this.parentAction = parentAction;
	}

	public List<Action> getChildActions()
	{
		return childActions;
	}

	public void addChild(Action action)
	{
		childActions.add(action);
		action.setParentAction(this);
		action.setSessionId(sessionId);
	}
}
