package at.jit.remind.core.model.content;

import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.model.Description;

public interface Source<S> extends Validate, Description, Comparable<Source<?>>
{
	S retrieve() throws MessageHandlerException;

	boolean isAlmostEqual(Source<?> that);
}
