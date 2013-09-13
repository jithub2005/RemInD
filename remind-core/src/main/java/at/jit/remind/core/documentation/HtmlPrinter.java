package at.jit.remind.core.documentation;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import at.jit.remind.core.exception.RemindModelException;
import at.jit.remind.core.xml.Description;
import at.jit.remind.core.xml.DistributionItem;
import at.jit.remind.core.xml.DocumentInformation;
import at.jit.remind.core.xml.InstallationBlock;
import at.jit.remind.core.xml.InstallationDocument;
import at.jit.remind.core.xml.InstallerInformation;

public class HtmlPrinter implements Printer<InstallationDocument>
{
	private ByteArrayOutputStream baos;

	public HtmlPrinter(ByteArrayOutputStream os)
	{
		this.baos = os;
	}

	@Override
	public void print(InstallationDocument model) throws PrinterException
	{
		generateVelocityContext(model);
	}

	/**
	 * Generate String object with HTML tags. We can transform it to plain HTML at the XHTML page using escape="true".
	 * 
	 * @param schema
	 *            Schema, unmarshalled XML data.
	 * @return HTML presentation of Schema object
	 * @throws RemindModelException
	 */
	private void generateVelocityContext(InstallationDocument installationDocument) throws PrinterException
	{
		VelocityEngine velocityEngine = new VelocityEngine();
		StringWriter writer = new StringWriter();
		try
		{
			velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
			velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
			velocityEngine.setProperty("runtime.log.logsystem.log4j.logger", "root");
			velocityEngine.init();

			Template template = velocityEngine.getTemplate("/work/systemSettings/resources/velocity.vm");
			VelocityContext velocityContext = new VelocityContext();
			// velocityContext.put("title", "HTMLtitle"); TODO No plan what this does. Maybe we can delete it in future.

			InstallerInformation installerInformation = installationDocument.getInstallerInformation();
			velocityContext.put("version", installerInformation.getVersion());

			DocumentInformation documentInformation = installationDocument.getDocumentInformation();
			velocityContext.put("header", documentInformation.getTitle());
			// velocityContext.put("version", documentInformation.getVersion()); TODO: check if we need this field any
			// more.
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

			baos.write(writer.toString().getBytes(Charset.forName("UTF-8")));

		}
		catch (Exception e) // NOSONAR Because VelocityEngine.init() throws Exception.
		{
			throw new PrinterException(e);
		}
	}
}
