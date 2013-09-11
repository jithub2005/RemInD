package at.jit.remind.core.model.content.database;

import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import at.jit.remind.core.connector.database.DbAccess;
import at.jit.remind.core.connector.database.DbAccess.JdbcConnectionFailedException;
import at.jit.remind.core.connector.database.DbAccess.JdbcConnectionTestException;
import at.jit.remind.core.connector.database.DbAccess.JdbcDriverNotFoundException;
import at.jit.remind.core.connector.database.DbAccess.SqlStatementException;
import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.ListBasedMessageHandler;

public class DBAccessTest
{
	private static final String jdbcDirver = "org.hsqldb.jdbcDriver";
	private static final String jdbcUrl = "jdbc:hsqldb:mem:dbtest";
	private static final String databaseUser = "sa";
	private static final String databasePassword = "";
	private static final String validationStatement = "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS;";

	private static ListBasedMessageHandler listBasedMessageHandler = new ListBasedMessageHandler();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		RemindContext.getInstance().setMessageHandler(listBasedMessageHandler);
	}

	@Test(expected = JdbcDriverNotFoundException.class)
	public void canHandleMissingJdbcDriver() throws JdbcDriverNotFoundException, JdbcConnectionFailedException, SqlStatementException
	{
		DbAccess dbAccess = new DbAccess(null, jdbcUrl, databaseUser, databasePassword);
		dbAccess.executeSqlStatement(validationStatement);
	}

	@Test(expected = JdbcConnectionFailedException.class)
	public void canHandleMissingJdbcUrl() throws JdbcDriverNotFoundException, JdbcConnectionFailedException, SqlStatementException
	{
		DbAccess dbAccess = new DbAccess(jdbcDirver, null, databaseUser, databasePassword);
		dbAccess.executeSqlStatement(validationStatement);
	}

	@Test(expected = JdbcConnectionFailedException.class)
	public void canHandleWrongUser() throws JdbcDriverNotFoundException, JdbcConnectionFailedException, SqlStatementException
	{
		DbAccess dbAccess = new DbAccess(jdbcDirver, jdbcUrl, "nn", databasePassword);
		dbAccess.executeSqlStatement(validationStatement);
	}

	@Test(expected = JdbcConnectionFailedException.class)
	public void canHandleWrongPassword() throws JdbcDriverNotFoundException, JdbcConnectionFailedException, SqlStatementException
	{
		DbAccess dbAccess = new DbAccess(jdbcDirver, jdbcUrl, databaseUser, "password");
		dbAccess.executeSqlStatement(validationStatement);
	}

	@Test
	public void canHandleCorrectTestStatement()
	{
		DbAccess dbAccess = new DbAccess(jdbcDirver, jdbcUrl, databaseUser, databasePassword);
		try
		{
			dbAccess.testConnection(validationStatement);
		}
		catch (JdbcConnectionTestException e)
		{
			fail("JdbcConnectionTestException should not occur.");
		}
	}

	@Test(expected = JdbcConnectionTestException.class)
	public void canHandleInvalidTestStatement() throws JdbcConnectionTestException
	{
		DbAccess dbAccess = new DbAccess(jdbcDirver, jdbcUrl, databaseUser, databasePassword);
		dbAccess.testConnection("SELECT * FROM FOO BAR");
	}
}
