package at.jit.remind.web.domain.context.reporting.service;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;

import at.jit.remind.web.domain.base.service.EntityServiceBase;
import at.jit.remind.web.domain.context.reporting.model.Action;

@Stateless
public class ActionService extends EntityServiceBase<Action>
{
	private static final long serialVersionUID = 8481446850596512080L;

	@PostConstruct
	@Override
	protected void initialize()
	{
		super.initialize();
		setEntityClazz(Action.class);
	}

	public List<Long> getCurrentAndChildActionIds(long actionId, List<Long> actionIds)
	{
		actionIds.add(actionId);
		Action parentAction = find(actionId);

		for (Action childAction : parentAction.getChildActions())
		{
			getCurrentAndChildActionIds(childAction.getId(), actionIds);
		}

		return actionIds;
	}

	public Action addChild(Action parent, Action child)
	{
		parent.addChild(child);

		return parent;
	}
}
