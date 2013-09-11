package at.jit.remind.core.documentation;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import at.jit.remind.core.context.CompositeKeyProvider;
import at.jit.remind.core.context.PropertiesProvider;
import at.jit.remind.core.context.RemindCompositeKeyProvider;
import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;
import at.jit.remind.core.exception.RemindModelException;
import at.jit.remind.core.xml.Description;
import at.jit.remind.core.xml.DistributionItem;
import at.jit.remind.core.xml.DocumentInformation;
import at.jit.remind.core.xml.InstallationBlock;
import at.jit.remind.core.xml.InstallationDocument;
import at.jit.remind.core.xml.Schema;

public class CreateHtmlDocumentation
{
	private static final String VelocityMappedKey = "format";

	private static final String HtmlOutputPath = "htmlOutputPath";
	private static final String VelocityPropertiesPath = "velocityPropertiesPath";

	private String generatedFileName = "";
	private String htmlOutputPath = "";

	private String velocityPropertiesPath = "";
	private String htmlContent = "";
	private Schema schema;

	public final static CompositeKeyProvider velocityKeyProvider = new RemindCompositeKeyProvider(CreateHtmlDocumentation.class.getSimpleName(),
			new String[]{VelocityMappedKey}, new String[]{HtmlOutputPath});

	public CreateHtmlDocumentation(String velocityId) throws RemindModelException
	{
		Map<String, String> lookupkeyMapping = new HashMap<String, String>();
		lookupkeyMapping.put(VelocityMappedKey, velocityId);

		PropertiesProvider propertiesProvider = RemindContext.getInstance().getPropertiesProvider();
		Properties connectionProperties = propertiesProvider.getProperties(velocityKeyProvider.getLookupId(lookupkeyMapping));

		if (connectionProperties.isEmpty())
		{
			RemindContext.getInstance().getMessageHandler()
					.addMessage(MessageLevel.ERROR, "No properties loaded!", "Is the name of the properties file correct?");

			throw new RemindModelException("No properties loaded!");
		}

		htmlOutputPath = connectionProperties.getProperty(HtmlOutputPath, "");
		velocityPropertiesPath = connectionProperties.getProperty(VelocityPropertiesPath, "");
	}

	public String generateVelocityContext(Schema schema) throws RemindModelException
	{
		this.schema = schema;
		VelocityEngine velocityEngine = new VelocityEngine();
		StringWriter writer = new StringWriter();
		try
		{
			velocityEngine.init(velocityPropertiesPath);
			Template template = velocityEngine.getTemplate(velocityEngine.getProperty("velocimacro.library").toString());
			VelocityContext velocityContext = new VelocityContext();
			// velocityContext.put("title", "HTMLtitle"); TODO No plan what this does. Maybe we can delete it in future.

			InstallationDocument installationDocument = schema.getInstallationDocument();
			DocumentInformation documentInformation = installationDocument.getDocumentInformation();
			velocityContext.put("header", documentInformation.getTitle());
			velocityContext.put("version", documentInformation.getVersion());
			velocityContext.put("target", documentInformation.getTarget());
			velocityContext.put("release", documentInformation.getRelease());
			velocityContext.put("tc", documentInformation.getTestCycle());

			List<DistributionItem> distributionList = new ArrayList<DistributionItem>(installationDocument.getDistributionItem());
			velocityContext.put("distributionList", distributionList);

			List<Description> descriptionOfContentList = new ArrayList<Description>(installationDocument.getDescriptionOfContent());
			velocityContext.put("descriptionOfContentList", descriptionOfContentList);

			List<Description> descriptionInstallationOrderList = new ArrayList<Description>(installationDocument.getDescriptionOfInstallationOrder());
			velocityContext.put("descriptionInstallationOrderList", descriptionInstallationOrderList);

			List<Description> descriptionPreconditionList = new ArrayList<Description>(installationDocument.getDescriptionOfPrecondition());
			velocityContext.put("descriptionPreconditionList", descriptionPreconditionList);

			List<InstallationBlock> installationBlockList = new ArrayList<InstallationBlock>(installationDocument.getInstallationBlock());
			velocityContext.put("installationBlockList", installationBlockList);

			template.merge(velocityContext, writer);

			RemindContext.getInstance().getMessageHandler().addMessage(writer.toString());

			generateResultFilePath();
		}
		catch (Exception e) // NOSONAR Because VelocityEngine.init() throws Exception.
		{
			RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.ERROR, "Velocity caused an exception!", e.getMessage());
			throw new RemindModelException(e);
		}

		htmlContent = writer.toString();

		return htmlContent;
	}

	public String writeHtmlFile() throws RemindModelException
	{
		String absolutFilePath = htmlOutputPath + "/" + getGeneratedHtmlFileName();
		if (!"".equals(htmlContent))
		{
			BufferedWriter bw = null;

			try
			{
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(absolutFilePath)), "UTF-8"));
				bw.write(htmlContent);
			}
			catch (IOException e)
			{
				RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.ERROR, "IO Exception occurred!", e.getMessage());
				throw new RemindModelException(e);
			}
			finally
			{
				try
				{
					if (bw != null)
					{
						bw.close();
					}
				}
				catch (IOException e)
				{
					RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.WARNING, "IO Exception occurred!", e.getMessage());
				}
			}
		}
		else
		{
			RemindContext.getInstance().getMessageHandler()
					.addMessage(MessageLevel.WARNING, "No HTML file generated!", "HTML content is empty. Have you already generated the velocity context?");
		}

		return absolutFilePath;
	}

	public String getGeneratedHtmlFileName()
	{
		return generatedFileName;
	}

	public OutputStream getHtmlContentAsOutputStream() throws RemindModelException
	{
		byte[] htmlContentAsByteArray = htmlContent.getBytes(Charset.forName("UTF-8"));
		ByteArrayOutputStream baos = new ByteArrayOutputStream(htmlContentAsByteArray.length);
		try
		{
			baos.write(htmlContentAsByteArray);
		}
		catch (IOException e)
		{
			RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.ERROR, "Cannot write HTML content to output stream.", e.getMessage());
			throw new RemindModelException(e);
		}

		return baos;
	}

	private void generateResultFilePath()
	{
		if (schema != null)
		{
			DocumentInformation d = schema.getInstallationDocument().getDocumentInformation();

			generatedFileName = d.getTarget() + "_" + d.getRelease() + "_" + d.getVersion() + "_" + getCurrentTime() + ".html";
			generatedFileName = generatedFileName.replace(" ", "_");
		}
		else
		{
			RemindContext.getInstance().getMessageHandler()
					.addMessage(MessageLevel.WARNING, "No HTML file name generated!", "No schema set. Have you already generated the velocity context?");
		}
	}

	private String getCurrentTime()
	{
		Date date = new Date();
		SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss");

		return sdf.format(date);
	}
}
