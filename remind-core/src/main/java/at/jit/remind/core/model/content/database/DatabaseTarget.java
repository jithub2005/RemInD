package at.jit.remind.core.model.content.database;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import at.jit.remind.core.connector.database.DbAccess;
import at.jit.remind.core.connector.database.DbAccess.JdbcConnectionTestException;
import at.jit.remind.core.context.CompositeKeyProvider;
import at.jit.remind.core.context.PropertiesProvider;
import at.jit.remind.core.context.RemindCompositeKeyProvider;
import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.model.content.Source;
import at.jit.remind.core.model.content.Target;

public class DatabaseTarget implements Target<File, SqlStatementList>
{
	private static final String SchemaKeyProviderSuffix = "User";

	public static final String SidMappedKey = "sid";
	public static final String SchemaMappedKey = "schema";
	public static final String EnvMappedKey = "environment";

	public static final String JdbcUrl = "jdbcUrl";
	public static final String JdbcDriver = "jdbcDriver";
	public static final String ValidationStatement = "validationStatement";

	public static final String User = "user";
	public static final String Password = "password";

	public static final CompositeKeyProvider connectionKeyProvider = new RemindCompositeKeyProvider(DatabaseTarget.class.getSimpleName(), new String[]{
			EnvMappedKey, SidMappedKey}, new String[]{JdbcUrl, JdbcDriver, ValidationStatement});

	public static final RemindCompositeKeyProvider schemaKeyProvider = new RemindCompositeKeyProvider(DatabaseTarget.class.getSimpleName()
			+ SchemaKeyProviderSuffix, new String[]{EnvMappedKey, SidMappedKey, SchemaMappedKey}, new String[]{User, Password});

	static
	{
		schemaKeyProvider.markContentAsEncrypted(Password);
	}

	private String jdbcUrl;
	private String jdbcDriver;
	private String user;
	private String password;
	private String validationStatement;

	private String environment;
	private String sid;
	private String schema;

	private Set<String> deploymentDetails = new LinkedHashSet<String>();

	public DatabaseTarget(String environment, String sid, String schema)
	{
		this.environment = environment;
		this.schema = schema;
		this.sid = sid;

		PropertiesProvider propertiesProvider = RemindContext.getInstance().getPropertiesProvider();

		Map<String, String> lookupkeyMapping = new HashMap<String, String>();
		lookupkeyMapping.put(EnvMappedKey, environment);
		lookupkeyMapping.put(SidMappedKey, sid);

		Properties connectionProperties = propertiesProvider.getProperties(connectionKeyProvider.getLookupId(lookupkeyMapping));
		jdbcUrl = connectionProperties.getProperty(JdbcUrl, "");
		jdbcDriver = connectionProperties.getProperty(JdbcDriver, "");
		validationStatement = connectionProperties.getProperty(ValidationStatement, "");

		lookupkeyMapping.put(SchemaMappedKey, schema);

		Properties schemaProperties = propertiesProvider.getProperties(schemaKeyProvider.getLookupId(lookupkeyMapping));
		user = schemaProperties.getProperty(User, "");
		password = schemaProperties.getProperty(Password, "");
	}

	@Override
	public void deploy(SqlStatementList sqlStatementList) throws MessageHandlerException
	{
		DbAccess dbAccess = new DbAccess(jdbcDriver, jdbcUrl, user, password);
		sqlStatementList.deploy(dbAccess, deploymentDetails);
	}

	@Override
	public Set<String> getDeploymentDetails()
	{
		return deploymentDetails;
	}

	@Override
	public SqlStatementList convert(Source<File> source) throws MessageHandlerException
	{
		File sqlFile = source.retrieve();
		SqlStatementList sqlStatementList = new SqlStatementList(sqlFile);
		RemindContext.getInstance().getMessageHandler().addMessage("Converting " + sqlFile.getAbsolutePath() + " to sql statement list.");
		sqlStatementList.convertSqlFileToStatements();

		return sqlStatementList;
	}

	@Override
	public void validate() throws MessageHandlerException
	{
		RemindContext.getInstance().getMessageHandler()
				.addMessage("Validating database access: jdbcDriver=" + jdbcDriver + ", jdbcUrl=" + jdbcUrl + ", user=" + user);
		// TODO Database-Connection auslagern.
		RemindContext.getInstance().getMessageHandler().addMessage("Testing connection...");
		DbAccess dbAccess = new DbAccess(jdbcDriver, jdbcUrl, user, password);

		try
		{
			dbAccess.testConnection(validationStatement);
		}
		catch (JdbcConnectionTestException e)
		{
			RemindContext
					.getInstance()
					.getMessageHandler()
					.addMessage(MessageLevel.ERROR,
							"Error while validating database access: jdbcDriver=" + jdbcDriver + ", jdbcUrl=" + jdbcUrl + ", user=" + user, e.getMessage());

			throw new MessageHandlerException(e);
		}

		RemindContext.getInstance().getMessageHandler().addMessage("Connection tested successfully.");
	}

	@Override
	public String getName()
	{
		return environment + ":" + sid + ":" + schema;
	}

	@Override
	public String getDetails()
	{
		StringBuffer b = new StringBuffer();
		b.append(connectionKeyProvider.getType());
		b.append('[').append(EnvMappedKey).append("='").append(environment).append("' ");
		b.append(SidMappedKey).append("='").append(sid).append("' ");
		b.append(SchemaMappedKey).append("='").append(schema).append("']");

		return b.toString();
	}

	@Override
	public int hashCode()
	{
		return getDetails().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof DatabaseTarget))
		{
			return false;
		}

		return getDetails().equals(((DatabaseTarget) obj).getDetails());
	}
}
