package at.jit.remind.web.ui.resource;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;

public class UncachedCssJsResourceHandler extends ResourceHandlerWrapper
{
	private ResourceHandler wrapped;

	public UncachedCssJsResourceHandler(ResourceHandler wrapped)
	{
		this.wrapped = wrapped;
	}

	@Override
	public ResourceHandler getWrapped()
	{
		return this.wrapped;
	}

	@Override
	public Resource createResource(String resourceName, String libraryName)
	{
		return new UncachedCssJsResource(wrapped.createResource(resourceName, libraryName));
	}
}
