package at.jit.remind.core.model.content.database;

import at.jit.remind.core.context.messaging.Feedback;

public class FixStatementFeedback implements Feedback
{
	private String sqlStatement; 
	
	public FixStatementFeedback()
	{
		
	}
	
	public FixStatementFeedback(String sqlStatement)
	{
		this.sqlStatement = sqlStatement;
	}
	
	@Override
	public String name() {
		// TODO Auto-generated method stub
		return FixStatementFeedback.class.getName();
	}

	public String getSqlStatement()
	{
		return sqlStatement;
	}
	
	public void setSqlStatement(String sqlStatement)
	{
		this.sqlStatement = sqlStatement;
	}
	
	public static String getName()
	{
		return FixStatementFeedback.class.getName(); 
	}
}
