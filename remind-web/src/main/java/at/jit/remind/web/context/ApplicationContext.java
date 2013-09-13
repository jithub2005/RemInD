package at.jit.remind.web.context;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.jar.Manifest;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.solder.logging.Logger;

@Named
@ApplicationScoped
public class ApplicationContext
{
	@Inject
	private Logger logger;

	private String version;
	private String versionKey;
	private String versionSuffix;

	private void fetchVersion()
	{
		ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();

		try
		{
			if (servletContext != null)
			{
				Manifest manifest = new Manifest(servletContext.getResourceAsStream("/META-INF/MANIFEST.MF"));
				version = manifest.getMainAttributes().getValue("Implementation-Version");
			}

			logger.info("ApplicationContext.version: " + version);

			StringTokenizer tokenizer = new StringTokenizer(version, "-");
			versionKey = tokenizer.hasMoreElements() ? tokenizer.nextToken() : version;
			versionSuffix = tokenizer.hasMoreElements() ? tokenizer.nextToken() : "";
		}
		catch (IOException e)
		{
			version = "Unknown";
		}
	}

	public String getVersion()
	{
		if (version == null)
		{
			fetchVersion();
		}

		return version;
	}

	public String getVersionKey()
	{
		if (version == null)
		{
			fetchVersion();
		}

		return versionKey;
	}

	public String getVersionSuffix()
	{
		if (version == null)
		{
			fetchVersion();
		}

		return versionSuffix;
	}

	public boolean isUserAgentIE()
	{
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

		return request.getHeader("User-Agent").contains("MSIE");
	}
}
