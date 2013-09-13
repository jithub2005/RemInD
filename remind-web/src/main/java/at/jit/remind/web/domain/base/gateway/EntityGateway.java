package at.jit.remind.web.domain.base.gateway;

import java.io.Serializable;

import at.jit.remind.web.domain.base.model.EntityBase;

public interface EntityGateway<E extends EntityBase> extends Serializable
{
	void save();

	E refresh(E entity);
}
