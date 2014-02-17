package at.jit.remind.core.model.content.database;

import static org.junit.Assert.assertSame;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.ListBasedMessageHandler;
import at.jit.remind.core.exception.MessageHandlerException;

public class SQLParserTest
{
	private static SqlParser sqlParser;
	private static ListBasedMessageHandler listBasedMessageHandler = new ListBasedMessageHandler();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		RemindContext.getInstance().setMessageHandler(listBasedMessageHandler);
		sqlParser = new SqlParser();
	}

	@After
	public void tearDown() throws Exception
	{
		listBasedMessageHandler.getMessageList().clear();
	}

	@Test(expected = MessageHandlerException.class)
	public void throwsMessageHandlerExceptionWhenFileIsNull() throws MessageHandlerException
	{
		sqlParser.parse(new SqlStatementList(null));
	}

	@Test(expected = MessageHandlerException.class)
	public void throwsMessageHandlerExceptionWhenFileIsUnknown() throws MessageHandlerException
	{
		sqlParser.parse(new SqlStatementList(new File("foobar.sql")));
	}

	@Test
	public void canHandleCommentsInSqlFileProperly() throws IOException, MessageHandlerException
	{
		SqlStatementList statementList = new SqlStatementList(setUpSqlFile("SQLParserTest1.sql"));
		sqlParser.parse(statementList);
		assertSame("Expected statement count of SQLParserTest1.sql is 12", 12, statementList.size());
	}

	@Test
	public void canHandlePlSqlAndJavaInSqlFile() throws IOException, MessageHandlerException
	{
		SqlStatementList statementList = new SqlStatementList(setUpSqlFile("SQLParserTest2OriginalOracle.sql"));
		sqlParser.parse(statementList);

		assertSame("Expected statement count of SQLParserTest2OriginalOracle.sql is 17", 17, statementList.size());
	}

	@Test
	public void canHandleSlashCommandInSqlFile() throws IOException, MessageHandlerException
	{
		SqlStatementList statementList = new SqlStatementList(setUpSqlFile("SQLParserTestSlash.sql"));
		sqlParser.parse(statementList);

		assertSame("Expected statement count of SQLParserTestSlash.sql is 20", 20, statementList.size());
	}

	@Test
	public void canHandleSlashCommandInSqlFile2() throws IOException, MessageHandlerException
	{
		SqlStatementList statementList = new SqlStatementList(setUpSqlFile("SQLParserTestSlash2.sql"));
		sqlParser.parse(statementList);

		assertSame("Expected statement count of SQLParserTestSlash.sql is 10", 10, statementList.size());
	}

	@Test
	public void canHandleCommentsOnTable() throws IOException, MessageHandlerException
	{
		SqlStatementList statementList = new SqlStatementList(setUpSqlFile("SQLParserTestTableComments.sql"));
		sqlParser.parse(statementList);

		//Rem. m.pitha: Changed size from 6 to 5 due to #1569. There are only 5 statements in this sql file. 
		//The semicolon in the last line belongs to the previous statement.
		assertSame("Expected statement count of SQLParserTestTableComments.sql is 5", 5, statementList.size());
	}
	
	@Test
	public void canHandleSingleLineCommentsWithHyphens() throws IOException, MessageHandlerException
	{
		SqlStatementList statementList = new SqlStatementList(setUpSqlFile("SQLParserTestSingleLineComment.sql"));
		sqlParser.parse(statementList);
		
		assertSame("Expected statement count of SQLParserTestTableComments.sql is 6", 6, statementList.size());
	}

	@Test
	public void canHandleCommentsInsideFunctionsInSqlFileProperly() throws IOException, MessageHandlerException
	{
		SqlStatementList statementList = new SqlStatementList(setUpSqlFile("SQLParserTestCommentsInsideFunction.sql"));
		sqlParser.parse(statementList);

		assertSame("Expected statement count of SQLParserTestCommentsInsideFunction.sql is 20", 20, statementList.size());
	}
	
	@Test
	public void canQuitCommandOnWithBackslash() throws MessageHandlerException, IOException
	{
		SqlStatementList statementList = new SqlStatementList(setUpSqlFile("SQLParserTestQuitCommentOnWithBackslash.sql"));
		sqlParser.parse(statementList);
		
		assertSame("Expected statement count of SQLParserTestTableComments.sql is 7", 7, statementList.size());		
	}
	
	@Test
	public void canHandleSemicolonInNextLine() throws MessageHandlerException, IOException
	{
		SqlStatementList statementList = new SqlStatementList(setUpSqlFile("SQLParserTestSemicolonInNextLine.sql"));
		sqlParser.parse(statementList);
		
		assertSame("Expected statement count of SQLParserTestSemicolonInNextLine.sql is 2", 2, statementList.size());	
	}
	
	private File setUpSqlFile(String fileName) throws IOException
	{
		String sqlFileAsString = FileUtils.readFileToString(FileUtils.toFile(DatabaseTargetTest.class.getResource("/database/" + fileName)));

		File tmpFile = File.createTempFile(getClass().getSimpleName(), ".tmp");
		tmpFile.deleteOnExit();

		BufferedWriter out = new BufferedWriter(new FileWriter(tmpFile));
		out.write(sqlFileAsString);
		out.close();

		return tmpFile;
	}
}
