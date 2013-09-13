package at.jit.remind.core.model.content;

import java.util.Set;

import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.model.Description;

public interface Target<S, T> extends Validate, Convert<S, T>, Description
{
	void deploy(T content) throws MessageHandlerException;

	Set<String> getDeploymentDetails();

	void validate() throws MessageHandlerException;
}
