package at.jit.remind.web.ui.controller.base;

import javax.enterprise.context.Conversation;
import javax.inject.Inject;

import at.jit.remind.web.domain.base.gateway.EntityGatewayBase;
import at.jit.remind.web.domain.base.model.EntityBase;

public abstract class EntityControllerBase<E extends EntityBase> implements EntityController<E>
{
	private static final long serialVersionUID = 8214302102294895378L;

	private EntityGatewayBase<E> entityGateway = null;

	private long id;
	private E entity;

	@Inject
	private Conversation conversation;

	protected void initialize()
	{
		// TODO: review and make abstract if method is not needed any more
	}

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public void setId(long id)
	{
		this.id = id;
		entity = null;
	}

	@Override
	public E getEntity()
	{
		if (entity == null)
		{
			if (id == 0)
			{
				entity = create();
			}
			else
			{
				entity = entityGateway.find(id);
			}
		}

		return entity;
	}

	protected abstract E create();

	public boolean isManaged()
	{
		return getId() != 0;
	}

	@Override
	public String save()
	{
		if (!isManaged())
		{
			entity = entityGateway.create(entity);
			id = entity.getId();
		}

		entityGateway.save();

		return "saved";
	}

	@Override
	public String cancel()
	{
		if (isManaged())
		{
			entity = entityGateway.refresh(entity);
		}
		else
		{
			entity = create();
		}

		return "canceled";
	}

	@Override
	public void delete(long id)
	{
		entityGateway.delete(id);

		if (this.id == id)
		{
			this.id = 0;
			entity = null;
		}
	}

	protected void beginConversation()
	{
		if (conversation.isTransient())
		{
			conversation.begin();
		}
	}

	protected void endConversation()
	{
		if (!conversation.isTransient())
		{
			conversation.end();
		}
	}

	protected Conversation getConversation()
	{
		return conversation;
	}

	protected EntityGatewayBase<E> getEntityGateway()
	{
		return entityGateway;
	}

	protected void setEntityGateway(EntityGatewayBase<E> entityGateway)
	{
		this.entityGateway = entityGateway;
	}
}
