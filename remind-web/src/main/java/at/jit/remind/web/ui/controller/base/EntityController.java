package at.jit.remind.web.ui.controller.base;

import java.io.Serializable;

import at.jit.remind.web.domain.base.model.EntityBase;

public interface EntityController<E extends EntityBase> extends Serializable
{
	long getId();

	void setId(long id);

	E getEntity();

	String save();

	String cancel();

	void delete(long id);
}
