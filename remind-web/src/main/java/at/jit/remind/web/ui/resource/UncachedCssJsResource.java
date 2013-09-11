package at.jit.remind.web.ui.resource;

import java.util.UUID;

import javax.faces.application.Resource;
import javax.faces.application.ResourceWrapper;

public class UncachedCssJsResource extends ResourceWrapper
{
	private static final String version = UUID.randomUUID().toString();

	private javax.faces.application.Resource resource;

	public UncachedCssJsResource(Resource resource)
	{
		this.resource = resource;
	}

	@Override
	public Resource getWrapped()
	{
		return this.resource;
	}

	@Override
	public String getRequestPath()
	{
		String requestPath = resource.getRequestPath();
		String requestUrl = resource.getURL().toExternalForm();

		if (requestUrl.contains("/resources/css") || requestUrl.contains("/resources/js"))
		{
			if (requestPath.contains("?"))
			{
				requestPath = new StringBuffer(requestPath).append("&rv=").append(version).toString();
			}
			else
			{
				requestPath = new StringBuffer(requestPath).append("?rv=").append(version).toString();
			}
		}

		return requestPath;
	}

	@Override
	public String getContentType()
	{
		return getWrapped().getContentType();
	}
}
