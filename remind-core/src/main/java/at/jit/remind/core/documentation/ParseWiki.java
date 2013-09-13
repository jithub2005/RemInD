package at.jit.remind.core.documentation;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import at.jit.remind.core.context.CompositeKeyProvider;
import at.jit.remind.core.context.PropertiesProvider;
import at.jit.remind.core.context.RemindCompositeKeyProvider;
import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;
import at.jit.remind.core.exception.RemindModelException;
import at.jit.remind.core.xml.Change;
import at.jit.remind.core.xml.Database;
import at.jit.remind.core.xml.Description;
import at.jit.remind.core.xml.FormattedDescription;
import at.jit.remind.core.xml.InstallationBlock;
import at.jit.remind.core.xml.Phase;
import at.jit.remind.core.xml.PhaseDescription;
import at.jit.remind.core.xml.Source;
import at.jit.remind.core.xml.SourceCodeManagement;
import at.jit.remind.core.xml.Subversion;
import at.jit.remind.core.xml.Target;

public class ParseWiki
{
	public static final String SyntaxMappedKey = "syntax";

	private static final String SvnRepo = "svnRepo";
	private static final String SvnTag = "svnTag";
	private static final String DbSchema = "dbSchema";
	private static final String Directory = "directory";
	private static final String TestCycle = "testCycle";

	private static final String MaintenanceKey = "maintenanceKey";
	private static final String EditTag = "editTag";

	private String sid = ""; // set by user in GUI.
	private List<Schema> schemaList = new ArrayList<ParseWiki.Schema>();

	public final static CompositeKeyProvider syntaxKeyProvider = new RemindCompositeKeyProvider(ParseWiki.class.getSimpleName(), new String[]{SyntaxMappedKey},
			new String[]{SvnRepo, SvnTag, DbSchema, Directory, TestCycle, MaintenanceKey, EditTag});

	private String svnRepo;
	private String svnTag;
	private String dbSchema;
	private String directory;
	private String testCycle;

	private String maintenanceKey;
	private String editTag;

	private String svnRepoValue;
	private String svnTagValue;
	private String testCycleValue;

	private boolean maintenanceKeyValue = false;

	private String syntaxId;

	public ParseWiki(String syntaxId)
	{
		this.syntaxId = syntaxId;
	}

	private String getSvnRepo(String line)
	{
		return findMatches(Pattern.quote(svnRepo) + "\\s*?:\\s*?([\\w\\/\\.:\\-]+)", line);
	}

	private String getSvnTag(String line)
	{
		return findMatches(Pattern.quote(svnTag) + "\\s*?:\\s*?([\\w\\\\/\\.:\\-]+)", line);
	}

	private String getSchema(String line)
	{
		return findMatches(Pattern.quote(editTag) + "\\s+?" + Pattern.quote(dbSchema) + "\\s*?:\\s*?([\\w]+)", line);
	}

	private String getFilesystem(String line)
	{
		return findMatches(Pattern.quote(directory) + "\\s*?:\\s*?([\\w\\\\/\\.:\\-]+)", line);
	}

	private InstallationParameters getSqlFileAndParameters(String line)
	{
		InstallationParameters param = new InstallationParameters();
		Pattern p = Pattern.compile("(\\\\[\\w\\\\/\\.\\-]+)\\s+?([JN])\\s+?([JN])\\s+?([JN])\\s+?([A-Z]+)", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(line);
		while (m.find())
		{
			param.setFilename(m.group(1) != null ? m.group(1) : "");
			param.setOnlyOnce("J".equalsIgnoreCase(m.group(2)) ? true : false);
			param.setAufQMinstallieren("J".equalsIgnoreCase(m.group(3)) ? true : false);
			param.setAufPRODinstalieren("J".equalsIgnoreCase(m.group(4)) ? true : false);
			param.setDeveloper(m.group(5) != null ? m.group(5) : "");
		}

		return param;
	}

	private String getTestCycle(String line)
	{
		return findMatches(testCycle + "\\s+?(\\d+)", line);
	}

	private String findMatches(String patternString, String line)
	{
		Pattern p = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(line);
		if (m.find())
		{
			return m.group(1);
		}

		return "";
	}

	private void checkLine(String line)
	{
		String repositoryUrlTemp = getSvnRepo(line);
		if (!"".equals(repositoryUrlTemp))
		{
			svnRepoValue = repositoryUrlTemp;
		}

		String svnTagTemp = getSvnTag(line);
		if (!"".equals(svnTagTemp))
		{
			svnTagValue = svnTagTemp;
		}

		if (line.toUpperCase(Locale.ENGLISH).contains(maintenanceKey.toUpperCase(Locale.ENGLISH)))
		{
			maintenanceKeyValue = true;
		}

		String schemaTemp = getSchema(line);
		if (!"".equals(schemaTemp))
		{
			schemaList.add(new Schema());
			schemaList.get(schemaList.size() - 1).setSchema(schemaTemp);
		}

		String fsTemp = getFilesystem(line);
		if (!"".equals(fsTemp) && !schemaList.isEmpty())
		{
			schemaList.get(schemaList.size() - 1).setFilesystem(fsTemp);
		}

		String tcTemp = getTestCycle(line);
		if (!"".equals(tcTemp))
		{
			testCycleValue = tcTemp;
		}

		if (line.startsWith("\\"))
		{
			InstallationParameters sqlPathAndParameters = getSqlFileAndParameters(line);
			if (!sqlPathAndParameters.isEmpty() && !schemaList.isEmpty())
			{
				schemaList.get(schemaList.size() - 1).getParamList().add(sqlPathAndParameters);
			}
		}

	}

	private String generateXML() throws JAXBException, IOException
	{
		JAXBContext context;

		OutputStream out = new ByteArrayOutputStream();
		InstallationBlock installationBlock = new InstallationBlock();
		if (maintenanceKeyValue)
		{
			context = JAXBContext.newInstance(InstallationBlock.class);
			installationBlock.setPhaseDescription(new PhaseDescription());
			installationBlock.getPhaseDescription().setPhase(Phase.DURING_MAINTENANCE);
			installationBlock.getPhaseDescription().getFormattedDescription().add(new FormattedDescription());
			installationBlock.getPhaseDescription().getFormattedDescription().get(0).setDescription(new Description());
			installationBlock.getPhaseDescription().getFormattedDescription().get(0).getDescription().setCaption("");
			installationBlock.getPhaseDescription().getFormattedDescription().get(0).getDescription().setText("");
			installationBlock.getPhaseDescription().getFormattedDescription().get(0).getFormat().add("");
		}
		else
		{
			context = JAXBContext.newInstance(Change.class);
		}

		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		for (Schema schema : schemaList)
		{
			for (InstallationParameters param : schema.getParamList())
			{
				Change change = new Change();
				change.setDescription("");
				change.setDeveloper(param.getDeveloper());
				change.setOnlyOnce(param.isOnlyOnce());
				if (param.isAufPRODinstalieren())
				{
					change.getEnvironment().add(at.jit.remind.core.xml.Environment.PRODUCTION);
				}
				if (param.isAufQMinstallieren())
				{
					change.getEnvironment().add(at.jit.remind.core.xml.Environment.QM);
				}
				change.setTestCycleNumber(new BigInteger(testCycleValue));
				change.setSource(new Source());
				change.getSource().setSourceCodeManagement(new SourceCodeManagement());
				change.getSource().getSourceCodeManagement().setSubversion(new Subversion());
				change.getSource().getSourceCodeManagement().getSubversion().setRepositoryUrl(svnRepoValue + "/" + svnTagValue);
				change.getSource().getSourceCodeManagement().getSubversion().setPath(schema.getFilesystem() + param.getFilename());
				change.getSource().getSourceCodeManagement().getSubversion().setRevision("-1");
				change.setTarget(new Target());
				change.getTarget().setDatabase(new Database());
				change.getTarget().getDatabase().setSchema(schema.getSchema());
				change.getTarget().getDatabase().setSID(sid);
				marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE); // prevents the line <?xml version...>
																				// from being generated.
				if (!maintenanceKeyValue)
				{
					marshaller.marshal(new JAXBElement<Change>(QName.valueOf(Change.class.getSimpleName()), Change.class, change), out);
				}
				else
				{
					installationBlock.getChange().add(change);
				}
			}
		}
		if (maintenanceKeyValue)
		{
			marshaller.marshal(new JAXBElement<InstallationBlock>(QName.valueOf(InstallationBlock.class.getSimpleName().toLowerCase()),
					InstallationBlock.class, installationBlock), out);
		}

		out.close();

		return out.toString();
	}

	private void initializeProperties() throws RemindModelException
	{
		Map<String, String> lookupkeyMapping = new HashMap<String, String>();
		lookupkeyMapping.put(SyntaxMappedKey, syntaxId);

		PropertiesProvider propertiesProvider = RemindContext.getInstance().getPropertiesProvider();
		Properties connectionProperties = propertiesProvider.getProperties(syntaxKeyProvider.getLookupId(lookupkeyMapping));

		if (connectionProperties.isEmpty()) // if the properties are empty, the file is not loaded properly.
		{
			RemindContext.getInstance().getMessageHandler()
					.addMessage(MessageLevel.ERROR, "No properties loaded!", "Is the name of the properties file correct?");

			throw new RemindModelException("No properties loaded!");
		}
		svnRepo = connectionProperties.getProperty(SvnRepo, "");
		svnTag = connectionProperties.getProperty(SvnTag, "");
		dbSchema = connectionProperties.getProperty(DbSchema, "");
		directory = connectionProperties.getProperty(Directory, "");
		testCycle = connectionProperties.getProperty(TestCycle, "");
		maintenanceKey = connectionProperties.getProperty(MaintenanceKey, "");
		editTag = connectionProperties.getProperty(EditTag, "");

		svnRepoValue = "";
		svnTagValue = "";
		testCycleValue = "";

		maintenanceKeyValue = false;

		schemaList.clear();
	}

	/**
	 * @param filePath
	 * @return
	 * @throws RemindModelException
	 * 
	 *             accepts a file which contains the content of a Wiki site and returns a xml file generated from this content.
	 */
	public String wikiFileToXml(File wikiFile) throws RemindModelException
	{
		initializeProperties();
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(wikiFile), "UTF-8"));
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				checkLine(line);
			}
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
				if (reader != null)
				{
					reader.close();
				}
			}
			catch (IOException e)
			{
				RemindContext.getInstance().getMessageHandler()
						.addMessage(MessageLevel.WARNING, "BufferedReader cannot be closed.", "BufferedReader cannot be closed.");
			}
		}

		String generatedXml = "";

		try
		{
			generatedXml = generateXML();
		}
		catch (JAXBException e)
		{
			RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.ERROR, "JAXBException occurred while generating xml file!", e.getMessage());
			throw new RemindModelException(e);
		}
		catch (IOException e)
		{
			RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.ERROR, "IO Exception occurred while generating xml file!", e.getMessage());
			throw new RemindModelException(e);
		}

		return generatedXml;
	}

	public void setSID(String sid)
	{
		this.sid = sid;
	}

	private static class Schema
	{
		private String filesystem = ""; // this is the sql file path
		private String schema = "";
		private List<InstallationParameters> paramList = new ArrayList<ParseWiki.InstallationParameters>();

		public String getFilesystem()
		{
			return filesystem;
		}

		public void setFilesystem(String filesystem)
		{
			this.filesystem = filesystem;
		}

		public String getSchema()
		{
			return schema;
		}

		public void setSchema(String schema)
		{
			this.schema = schema;
		}

		public List<InstallationParameters> getParamList()
		{
			return paramList;
		}
	}

	private static class InstallationParameters
	{
		private String filename = ""; // has to be concatenated with directory.
		private String developer = "";
		private boolean onlyOnce = false;
		private boolean aufQMinstallieren = false;
		private boolean aufPRODinstalieren = false;

		public String getFilename()
		{
			return filename;
		}

		public void setFilename(String filename)
		{
			this.filename = filename;
		}

		public String getDeveloper()
		{
			return developer;
		}

		public void setDeveloper(String developer)
		{
			this.developer = developer;
		}

		public boolean isOnlyOnce()
		{
			return onlyOnce;
		}

		public void setOnlyOnce(boolean onlyOnce)
		{
			this.onlyOnce = onlyOnce;
		}

		public boolean isAufQMinstallieren()
		{
			return aufQMinstallieren;
		}

		public void setAufQMinstallieren(boolean aufQMinstallieren)
		{
			this.aufQMinstallieren = aufQMinstallieren;
		}

		public boolean isAufPRODinstalieren()
		{
			return aufPRODinstalieren;
		}

		public void setAufPRODinstalieren(boolean aufPRODinstalieren)
		{
			this.aufPRODinstalieren = aufPRODinstalieren;
		}

		public boolean isEmpty()
		{
			if ("".equals(getFilename()) || "".equals(getDeveloper()))
			{
				return true;
			}

			return false;
		}
	}
}
