package at.jit.remind.core.model.content.database;

import at.jit.remind.core.context.messaging.Feedback;

public class StatementFixedFeedback implements Feedback
{
	private String sqlStatement; 
	
	public StatementFixedFeedback()
	{
		
	}
	
	public StatementFixedFeedback(String sqlStatement)
	{
		this.sqlStatement = sqlStatement;
	}
	
	@Override
	public String name() {
		// TODO Auto-generated method stub
		return StatementFixedFeedback.class.getName();
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
		return StatementFixedFeedback.class.getName(); 
	}
}
