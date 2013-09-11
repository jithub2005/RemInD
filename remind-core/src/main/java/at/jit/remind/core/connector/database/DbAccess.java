package at.jit.remind.core.connector.database;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Locale;

import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;

public class DbAccess
{
	private Connection con = null;
	private String jdbcDriver;
	private String jdbcUrl;
	private String user;
	private String passwd;

	public DbAccess(String jdbcDriver, String jdbcUrl, String user, String passwd)
	{
		this.jdbcDriver = (jdbcDriver != null ? jdbcDriver : "");
		this.user = (user != null ? user : "");
		this.passwd = (passwd != null ? passwd : "");
		this.jdbcUrl = (jdbcUrl != null ? jdbcUrl : "");
		// TODO logOnly muss noch implementiert werden.
	}

	private void loadDriver(String jdbcDriver) throws JdbcDriverNotFoundException
	{
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements())
		{
			Driver driver = drivers.nextElement();
			if (driver.getClass().getName().equals(jdbcDriver))
			{
				return;
			}
		}

		try
		{
			Class.forName(jdbcDriver);
		}
		catch (ClassNotFoundException e)
		{
			throw new JdbcDriverNotFoundException(e);
		}
	}

	private Connection getConnection() throws JdbcDriverNotFoundException, JdbcConnectionFailedException
	{
		// TODO if logOnly == true -> return
		loadDriver(jdbcDriver);

		try
		{
			con = DriverManager.getConnection(jdbcUrl.toLowerCase(Locale.ENGLISH), user.toLowerCase(Locale.ENGLISH), passwd);
		}
		catch (SQLException e)
		{
			throw new JdbcConnectionFailedException(e);
		}

		return con;
	}

	// // Our statement string is indeed non constant, because it's parsed by the sql parser.
	// // So we cannot use prepared statements here. (If we do so, FindBugs will still complain about the non constant
	// // string)
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
	public void executeSqlStatement(final String stmt) throws JdbcDriverNotFoundException, JdbcConnectionFailedException, SqlStatementException
	{
		// if logOnly == true -> return

		if (stmt == null || stmt.isEmpty() || "/".equals(stmt))
		{
			RemindContext.getInstance().getMessageHandler().addMessage("Ignoring statement: " + stmt);
			return;
		}

		Statement sqlStatement = null;

		con = getConnection();

		try
		{
			sqlStatement = con.createStatement();
			RemindContext.getInstance().getMessageHandler().addMessage("Executing statement: " + stmt);
			sqlStatement.execute(stmt);
			RemindContext.getInstance().getMessageHandler().addMessage("Done.");
		}
		catch (SQLException e)
		{
			throw new SqlStatementException(e);
		}
		finally
		{
			try
			{
				if (sqlStatement != null)
				{
					sqlStatement.close();
				}
			}
			catch (SQLException e)
			{
				RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.WARNING, "SQLStatement could not be closed.", e.getMessage());
			}

			closeConnection();
		}
	}

	// Our statement string is indeed non constant, because it's parsed by the sql parser.
	// So we cannot use prepared statements here. (If we do so, FindBugs will still complain about the non constant
	// string)
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
	public void testConnection(String stmt) throws JdbcConnectionTestException
	{
		try
		{
			con = getConnection();
		}
		catch (JdbcDriverNotFoundException e)
		{
			throw new JdbcConnectionTestException(e);
		}
		catch (JdbcConnectionFailedException e)
		{
			throw new JdbcConnectionTestException(e);
		}

		Statement sqlStatement = null;
		try
		{
			sqlStatement = con.createStatement();
			sqlStatement.execute(stmt);
		}
		catch (SQLException e)
		{
			throw new JdbcConnectionTestException(e);
		}
		finally
		{
			try
			{
				if (sqlStatement != null)
				{
					sqlStatement.close();
				}
			}
			catch (SQLException e1)
			{
				RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.WARNING, "SQLStatement could not be closed.", e1.getMessage());
			}

			closeConnection();
		}
	}

	private void closeConnection()
	{
		try
		{
			if (con != null)
			{
				con.close();
			}
		}
		catch (SQLException e)
		{
			RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.WARNING, "Database connection cannot be closed.", e.getMessage());
		}
	}

	public static final class JdbcDriverNotFoundException extends Exception
	{
		private static final long serialVersionUID = 7475737989624284824L;

		public JdbcDriverNotFoundException(Throwable t)
		{
			super(t);
		}
	}

	public static final class JdbcConnectionFailedException extends Exception
	{
		private static final long serialVersionUID = 1751767536843635267L;

		public JdbcConnectionFailedException(Throwable t)
		{
			super(t);
		}
	}

	public static final class JdbcConnectionTestException extends Exception
	{
		private static final long serialVersionUID = -8766223966849736367L;

		public JdbcConnectionTestException(Throwable t)
		{
			super(t);
		}
	}

	public static final class SqlStatementException extends Exception
	{
		private static final long serialVersionUID = -5094218800331184714L;

		public SqlStatementException(Throwable t)
		{
			super(t);
		}
	}
}
