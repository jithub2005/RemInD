package at.jit.remind.core.model.content;

import at.jit.remind.core.exception.MessageHandlerException;

public interface Convert<S, T>
{
	T convert(Source<S> source) throws MessageHandlerException;
}
