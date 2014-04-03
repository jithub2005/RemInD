package at.jit.remind.core.model.content.database;

import static org.junit.Assert.assertSame;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.ListBasedMessageHandler;
import at.jit.remind.core.exception.MessageHandlerException;

@RunWith(PowerMockRunner.class)
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
    public void canHandleSimpleStatements() throws MessageHandlerException, IOException
    {
        SqlStatementList sqlStatementList = new SqlStatementList(setUpSqlFile("SQLParserTestEndTagBehaviour.sql"));
        sqlParser.parse(sqlStatementList);

        assertSame("Expected statement count of SQLParserTestEndTagBehaviour.sql is 10", 10, sqlStatementList.size());       
    }

    @Test
    public void canHandleCommentsInSqlFileProperly() throws IOException, MessageHandlerException
    {
        SqlStatementList sqlStatementList = new SqlStatementList(setUpSqlFile("SQLParserTest1.sql"));
        sqlParser.parse(sqlStatementList);

        assertSame("Expected statement count of SQLParserTest1.sql is 12", 12, sqlStatementList.size());
    }

    @Test
    public void canHandlePlSqlAndCreateOrReplaceAndCompileJava() throws IOException, MessageHandlerException
    {
        SqlStatementList statementList = new SqlStatementList(setUpSqlFile("SQLParserTest2OriginalOracle.sql"));
        sqlParser.parse(statementList);

        assertSame("Expected statement count of SQLParserTest2OriginalOracle.sql is 17", 17, statementList.size());
    }

    @Test
    public void canHandlePlSqlAndCreateOrReplaceAndResolveJava() throws IOException, MessageHandlerException
    {
        SqlStatementList statementList = new SqlStatementList(setUpSqlFile("SQLParserTestCreateOrReplaceAndResolve.sql"));
        sqlParser.parse(statementList);

        assertSame("Expected statement count of SQLParserTestCreateOrReplaceAndResolve.sql is 11", 11, statementList.size());
    }

    @Test
    public void canHandleJavadocCommentsInJavaCode() throws MessageHandlerException, IOException
    {
        SqlStatementList statementList = new SqlStatementList(setUpSqlFile("SQLParserTestJavaStatementWithJavadoc.sql"));
        sqlParser.parse(statementList);

        assertSame("Expected statement count of SQLParserTestJavaStatementWithJavadoc.sql is 2", 2, statementList.size());
    }

    @Test
    public void canHandleMultilineDocCommentsInJavaCode() throws MessageHandlerException, IOException
    {
        SqlStatementList statementList = new SqlStatementList(setUpSqlFile("SQLParserTestJavaStatementWithMultilineDoc.sql"));
        sqlParser.parse(statementList);

        assertSame("Expected statement count of SQLParserTest2OriginalOracle.sql is 2", 2, statementList.size());
    }

    @Test
    public void canHandleSingleLineCommentsInJavaCode() throws MessageHandlerException, IOException
    {
        SqlStatementList statementList = new SqlStatementList(setUpSqlFile("SQLParserTestJavaStatementWithSingleLineDoc.sql"));
        sqlParser.parse(statementList);

        assertSame("Expected statement count of SQLParserTest2OriginalOracle.sql is 2", 2, statementList.size());
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

        // Rem. m.pitha: Changed size from 6 to 5 due to #1569. There are only 5
        // statements in this sql file.
        // The semicolon in the last line belongs to the previous statement.
        assertSame("Expected statement count of SQLParserTestTableComments.sql is 5", 5, statementList.size());
    }

    @Test
    public void canHandleSingleLineCommentsWithHyphens() throws IOException, MessageHandlerException
    {
        SqlStatementList sqlStatementList = new SqlStatementList(setUpSqlFile("SQLParserTestSingleLineComment.sql"));
        sqlParser.parse(sqlStatementList);

        @SuppressWarnings("unchecked")
        List<AtomicSqlStatement> atomicSqlStatements = (List<AtomicSqlStatement>) Whitebox.getInternalState(sqlStatementList, "statementList");
        AtomicSqlStatement atomicSqlStatement = atomicSqlStatements.get(2);

        boolean onlyComment = Whitebox.getInternalState(atomicSqlStatement, "onlyComment");

        assertSame("Expected statement count of SQLParserTestTableComments.sql is 6", 6, sqlStatementList.size());
        assertSame("It's not enough to recognize the comment as atomic statement. The onlyComment parameter must be true too.", true, onlyComment);
    }

    @Test
    public void canHandleSingleLineCommentsAtFirstLine() throws MessageHandlerException, IOException
    {
        SqlStatementList statementList = new SqlStatementList(setUpSqlFile("SQLParserTestSingleCommentFirstLine.sql"));
        sqlParser.parse(statementList);

        assertSame("Expected statement count of SQLParserTestSingleCommentFirstLine.sql is 2", 2, statementList.size());
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

    @Test
    public void canHandleMultiLineStatements() throws MessageHandlerException, IOException
    {
        SqlStatementList statementList = new SqlStatementList(setUpSqlFile("SQLParserTestMultiLineInsert.sql"));
        sqlParser.parse(statementList);

        assertSame("Expected statement count of SQLParserTestMultiLineInsert.sql is 6", 6, statementList.size());
    }

    @Test
    public void canHandleMultipleMultiLineCommentsInOneLine() throws MessageHandlerException, IOException
    {
        SqlStatementList sqlStatementList = new SqlStatementList(setUpSqlFile("SQLParserTestLineComment.sql"));
        sqlParser.parse(sqlStatementList);

        @SuppressWarnings("unchecked")
        List<AtomicSqlStatement> atomicSqlStatements = (List<AtomicSqlStatement>) Whitebox.getInternalState(sqlStatementList, "statementList");
        AtomicSqlStatement atomicSqlStatement1 = atomicSqlStatements.get(0);
        AtomicSqlStatement atomicSqlStatement2 = atomicSqlStatements.get(1);
        AtomicSqlStatement atomicSqlStatement4 = atomicSqlStatements.get(3);

        boolean onlyComment1 = Whitebox.getInternalState(atomicSqlStatement1, "onlyComment");
        boolean onlyComment2 = Whitebox.getInternalState(atomicSqlStatement2, "onlyComment");
        boolean onlyComment4 = Whitebox.getInternalState(atomicSqlStatement4, "onlyComment");

        assertSame("It's not enough to recognize the comment as atomic statement. The onlyComment parameter must be true too.", true, onlyComment1);
        assertSame("It's not enough to recognize the comment as atomic statement. The onlyComment parameter must be true too.", true, onlyComment2);
        assertSame("It's not enough to recognize the comment as atomic statement. The onlyComment parameter must be true too.", true, onlyComment4);
        
        assertSame("Expected statement count of SQLParserTestMultiLineInsert.sql is 7", 7, sqlStatementList.size());
    }

    @Test
    public void canHandleMultiLineCommentInOneRow() throws MessageHandlerException, IOException
    {
        SqlStatementList sqlStatementList = new SqlStatementList(setUpSqlFile("SQLParserTestLineComment2.sql"));
        sqlParser.parse(sqlStatementList);

        @SuppressWarnings("unchecked")
        List<AtomicSqlStatement> atomicSqlStatements = (List<AtomicSqlStatement>) Whitebox.getInternalState(sqlStatementList, "statementList");
        AtomicSqlStatement atomicSqlStatement = atomicSqlStatements.get(0);

        boolean onlyComment = Whitebox.getInternalState(atomicSqlStatement, "onlyComment");

        assertSame("It's not enough to recognize the comment as atomic statement. The onlyComment parameter must be true too.", true, onlyComment);
    }
    
    @Test
    public void canHandleSingleLineCommentsWithinSqlStatement() throws MessageHandlerException, IOException
    {
        SqlStatementList sqlStatementList = new SqlStatementList(setUpSqlFile("SQLParserTestSingleCommentWithinStatement.sql"));
        sqlParser.parse(sqlStatementList);
        
        @SuppressWarnings("unchecked")
        List<AtomicSqlStatement> atomicSqlStatements = (List<AtomicSqlStatement>) Whitebox.getInternalState(sqlStatementList, "statementList");
        AtomicSqlStatement atomicSqlStatement = atomicSqlStatements.get(2);

        boolean onlyComment = Whitebox.getInternalState(atomicSqlStatement, "onlyComment");

        assertSame("Expected statement count of SQLParserTestMultiLineInsert.sql is 5", 5, sqlStatementList.size());
        assertSame("onlyComment must not be true, because it's a comment within a sql statement.", false, onlyComment);
    }
    
    @Test
    public void canHandleMultiLineCommentsWithinSqlStatement() throws MessageHandlerException, IOException
    {
        SqlStatementList sqlStatementList = new SqlStatementList(setUpSqlFile("SQLParserTestMultilineCommentWithinStatement.sql"));
        sqlParser.parse(sqlStatementList);
        
        @SuppressWarnings("unchecked")
        List<AtomicSqlStatement> atomicSqlStatements = (List<AtomicSqlStatement>) Whitebox.getInternalState(sqlStatementList, "statementList");
        AtomicSqlStatement atomicSqlStatement = atomicSqlStatements.get(2);

        boolean onlyComment = Whitebox.getInternalState(atomicSqlStatement, "onlyComment");

        assertSame("Expected statement count of SQLParserTestMultiLineInsert.sql is 5", 5, sqlStatementList.size());
        assertSame("onlyComment must not be true, because it's a comment within a sql statement.", false, onlyComment);
    }
    
    @Test
    public void canHandleStatementWithSemicolonInNextLineInPackage() throws MessageHandlerException, IOException
    {
        SqlStatementList sqlStatementList = new SqlStatementList(setUpSqlFile("SQLParserTestSemicolonNextLinePackage.sql"));
        sqlParser.parse(sqlStatementList);
        
        @SuppressWarnings("unchecked")
        List<AtomicSqlStatement> atomicSqlStatements = (List<AtomicSqlStatement>) Whitebox.getInternalState(sqlStatementList, "statementList");
        AtomicSqlStatement atomicSqlStatement = atomicSqlStatements.get(0);

        boolean onlyComment = Whitebox.getInternalState(atomicSqlStatement, "onlyComment");

        assertSame("Expected statement count of SQLParserTestSemicolonNextLinePackage.sql is 2", 2, sqlStatementList.size());
        assertSame("onlyComment must not be true, because it's a common sql statement.", false, onlyComment);
    }
    
    @Test
    public void canHandleStatementWithSemicolonInNextLineInProcedure() throws MessageHandlerException, IOException
    {
        SqlStatementList sqlStatementList = new SqlStatementList(setUpSqlFile("SQLParserTestSemicolonNextLineProcedure.sql"));
        sqlParser.parse(sqlStatementList);
        
        @SuppressWarnings("unchecked")
        List<AtomicSqlStatement> atomicSqlStatements = (List<AtomicSqlStatement>) Whitebox.getInternalState(sqlStatementList, "statementList");
        AtomicSqlStatement atomicSqlStatement = atomicSqlStatements.get(0);
        
        boolean onlyComment = Whitebox.getInternalState(atomicSqlStatement, "onlyComment");

        assertSame("Expected statement count of SQLParserTestSemicolonNextLineProcedure.sql is 2", 2, sqlStatementList.size());
        assertSame("onlyComment must not be true, because it's a common sql statement.", false, onlyComment);
    }
    
    @Test
    public void canHandleCommentWithinPlSql() throws MessageHandlerException, IOException
    {
        SqlStatementList sqlStatementList = new SqlStatementList(setUpSqlFile("SQLParserTestCommentInPlSql.sql"));
        sqlParser.parse(sqlStatementList);
        
        @SuppressWarnings("unchecked")
        List<AtomicSqlStatement> atomicSqlStatements = (List<AtomicSqlStatement>) Whitebox.getInternalState(sqlStatementList, "statementList");
        AtomicSqlStatement atomicSqlStatement = atomicSqlStatements.get(0);
        
        boolean onlyComment = Whitebox.getInternalState(atomicSqlStatement, "onlyComment");

        assertSame("Expected statement count of SQLParserTestCommentInPlSql.sql is 2", 2, sqlStatementList.size());      
        assertSame("onlyComment must not be true, because it's a common sql statement.", false, onlyComment);
    }
    
    @Test
    public void canHandleStatementWithSemicolonInNextLineWithinPlSql() throws MessageHandlerException, IOException
    {
        SqlStatementList sqlStatementList = new SqlStatementList(setUpSqlFile("SQLParserTestSemicolonNextLinePlSql.sql"));
        sqlParser.parse(sqlStatementList);
        
        @SuppressWarnings("unchecked")
        List<AtomicSqlStatement> atomicSqlStatements = (List<AtomicSqlStatement>) Whitebox.getInternalState(sqlStatementList, "statementList");
        AtomicSqlStatement atomicSqlStatement = atomicSqlStatements.get(0);
        
        boolean onlyComment = Whitebox.getInternalState(atomicSqlStatement, "onlyComment");

        assertSame("Expected statement count of SQLParserTestCommentInPlSql.sql is 2", 2, sqlStatementList.size());  
        assertSame("onlyComment must not be true, because it's a common sql statement.", false, onlyComment);
    }
    
    @Test
    public void canHandleStatementWithSemicolonInSameLineWithinPlSql() throws MessageHandlerException, IOException
    {
        SqlStatementList sqlStatementList = new SqlStatementList(setUpSqlFile("SQLParserTestSemicolonSameLineWithinPlSql.sql"));
        sqlParser.parse(sqlStatementList);

        @SuppressWarnings("unchecked")
        List<AtomicSqlStatement> atomicSqlStatements = (List<AtomicSqlStatement>) Whitebox.getInternalState(sqlStatementList, "statementList");
        AtomicSqlStatement atomicSqlStatement = atomicSqlStatements.get(0);
        
        boolean onlyComment = Whitebox.getInternalState(atomicSqlStatement, "onlyComment");

        assertSame("Expected statement count of SQLParserTestSemicolonSameLineWithinPlSql.sql is 2", 2, sqlStatementList.size());
        assertSame("onlyComment must not be true, because it's a common sql statement.", false, onlyComment);
    }
    
    @Test
    public void canHandleNestedBlocksWithSemicolonsInNextLineAndComments() throws MessageHandlerException, IOException
    {
        SqlStatementList sqlStatementList = new SqlStatementList(setUpSqlFile("SQLParserTestNestedBlocksSemicolonsComments.sql"));
        sqlParser.parse(sqlStatementList);
        
        @SuppressWarnings("unchecked")
        List<AtomicSqlStatement> atomicSqlStatements = (List<AtomicSqlStatement>) Whitebox.getInternalState(sqlStatementList, "statementList");
        AtomicSqlStatement atomicSqlStatement = atomicSqlStatements.get(0);
        
        boolean onlyComment = Whitebox.getInternalState(atomicSqlStatement, "onlyComment");

        assertSame("Expected statement count of SQLParserTestNestedBlocksSemicolonsComments.sql is 2", 2, sqlStatementList.size());
        assertSame("onlyComment must not be true, because it's a common sql statement.", false, onlyComment);
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
