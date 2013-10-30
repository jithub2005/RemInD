package at.jit.remind.core.model.content.database;

import java.util.ArrayList;
import java.util.List;

import at.jit.remind.core.connector.database.DbAccess;
import at.jit.remind.core.connector.database.DbAccess.JdbcConnectionFailedException;
import at.jit.remind.core.connector.database.DbAccess.JdbcDriverNotFoundException;
import at.jit.remind.core.connector.database.DbAccess.SqlStatementException;
import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.Feedback;
import at.jit.remind.core.context.messaging.FeedbackContext;
import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.model.RemindModelFeedback;

public class AtomicSqlStatement
{
	private SqlStatementList statementList;

	private String singleSqlStatement;
	private boolean onlyComment;
	private boolean defective = false; // TODO Vielleicht nicht benötigt.
	private int statementIndexFrom = -1;
	private int statementIndexTo = -1;
	private boolean gotCorrected = false;
	
	private List<String> correctedSqlStatements;

	public AtomicSqlStatement(SqlStatementList statementList, String singleSqlStatement)
	{
		this(statementList, singleSqlStatement, false);
	}

	public AtomicSqlStatement(SqlStatementList statementList, String singleSqlStatement, boolean onlyComment)
	{
		this.statementList = statementList;
		this.statementList.addAtomicStatement(this);
		this.singleSqlStatement = singleSqlStatement.trim();
		this.onlyComment = onlyComment;
		this.correctedSqlStatements = new ArrayList<String>();
	}

	public void deploy(DbAccess dbAccess) throws MessageHandlerException
	{
		if (onlyComment)
		{
			RemindContext.getInstance().getMessageHandler().addMessage("Skipped comment statement: " + singleSqlStatement);

			return;
		}

		try
		{
			deployStatement(dbAccess, singleSqlStatement);
		}
		catch(MessageHandlerException e)
		{
			if(FixStatementFeedback.getName().equals(e.getFeedback().name()))
			{
				FixStatementFeedback feedback = (FixStatementFeedback) e.getFeedback();
				try
				{
					deployCorrectedSqlStatement(feedback.getSqlStatement(), dbAccess);
				}
				catch(MessageHandlerException mhe)
				{
					throw mhe;
				}
			}
			else
			{				
				throw e;
			}
		}
	}
	
	private void deployCorrectedSqlStatement(String correctedSql, DbAccess dbAccess) throws MessageHandlerException
	{
		this.defective = true;
		String toDeploy = correctedSql;
		boolean isCorrected = false;
		do 
		{
			correctedSqlStatements.add(toDeploy);
			try
			{
				deployStatement(dbAccess, toDeploy);
				gotCorrected = true;
				isCorrected = true;
			}
			catch(MessageHandlerException e)
			{
				if(! FixStatementFeedback.getName().equals(e.getFeedback().name()))
				{
					throw e;
				}
				else
				{
					FixStatementFeedback feedback = (FixStatementFeedback) e.getFeedback();
					toDeploy = feedback.getSqlStatement();
				}
			}
		}
		while(!isCorrected);
		RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.WARNING, "SQL statement fixed!", "");
	}
	
	private void deployStatement(DbAccess dbAccess, String sql) throws MessageHandlerException
	{
		try
		{
			dbAccess.executeSqlStatement(sql);
		}
		catch (JdbcDriverNotFoundException e)
		{
			FeedbackContext feedbackContext = new FeedbackContext.Builder().withFeedback(RemindModelFeedback.Abort, "Abort deployment")
					.withFeedback(RemindModelFeedback.Skip, "Skip file")
					.withData(FeedbackContext.soureFilePathDataKey, statementList.getFile().getAbsolutePath())
					.withData(FeedbackContext.errorCauseDataKey, singleSqlStatement).build();
			Feedback feedback = RemindContext.getInstance().getMessageHandler()
					.addMessageWithFeedback(MessageLevel.ERROR, "Failed to load jdbc driver", e.getMessage(), feedbackContext);

			throw new MessageHandlerException(e, feedback);
		}
		catch (JdbcConnectionFailedException e)
		{
			FeedbackContext feedbackContext = new FeedbackContext.Builder().withFeedback(RemindModelFeedback.Abort, "Abort deployment")
					.withFeedback(RemindModelFeedback.Skip, "Skip file")
					.withData(FeedbackContext.soureFilePathDataKey, statementList.getFile().getAbsolutePath())
					.withData(FeedbackContext.errorCauseDataKey, singleSqlStatement).build();
			Feedback feedback = RemindContext.getInstance().getMessageHandler()
					.addMessageWithFeedback(MessageLevel.ERROR, "Failed to connect to database", e.getMessage(), feedbackContext);

			throw new MessageHandlerException(e, feedback);
		}
		catch (SqlStatementException e)
		{
			FeedbackContext feedbackContext = new FeedbackContext.Builder().withFeedback(RemindModelFeedback.Abort, "Abort deployment")
					.withFeedback(RemindModelFeedback.Skip, "Skip file").withFeedback(DatabaseFeedback.SkipStatement, "Skip statement")
					.withFeedback(new FixStatementFeedback(), "Fix Statement")
					.withData(FeedbackContext.soureFilePathDataKey, statementList.getFile().getAbsolutePath())
					.withData(FeedbackContext.errorCauseDataKey, sql)
					.withData(FeedbackContext.containsFixFeedbackKey, "true").build();
			Feedback feedback = RemindContext.getInstance().getMessageHandler()
					.addMessageWithFeedback(MessageLevel.ERROR, "SQLError: " + sql, e.getMessage(), feedbackContext);

			throw new MessageHandlerException(e, feedback);
		}
	}

	public boolean isDefective()
	{
		return defective;
	}

	public void setDefective()
	{
		this.defective = true;
	}

	public int getStatementIndexFrom()
	{
		return statementIndexFrom;
	}

	public void setStatementIndexFrom(int statementIndexFrom)
	{
		this.statementIndexFrom = statementIndexFrom;
	}

	public int getStatementIndexTo()
	{
		return statementIndexTo;
	}

	public void setStatementIndexTo(int statementIndexTo)
	{
		this.statementIndexTo = statementIndexTo;
	}

	public int getLength()
	{
		return singleSqlStatement.length();
	}
	
	public void setCorrectedSqlStatements(List<String> correctedSqlStatements)
	{
		this.correctedSqlStatements = correctedSqlStatements;
	}
	
	public List<String> getCorrectedSqlStatements()
	{
		return this.correctedSqlStatements;
	}
	
	public Boolean gotCorrected()
	{
		return gotCorrected;
	}

	@Override
	public String toString()
	{
		return singleSqlStatement;
	}
}
