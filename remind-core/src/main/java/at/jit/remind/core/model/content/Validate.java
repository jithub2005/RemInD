package at.jit.remind.core.model.content;

import at.jit.remind.core.exception.MessageHandlerException;

public interface Validate
{
	void validate() throws MessageHandlerException;
}
