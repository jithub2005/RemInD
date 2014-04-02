package at.jit.remind.core.model.content.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;

import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.xml.EndTag;
import at.jit.remind.core.xml.ParserDefinitionDocument;
import at.jit.remind.core.xml.ParserSchema;
import at.jit.remind.core.xml.StartTag;

public class SqlParser
{
	private ParserSchema parserSchema;
	private static String commentStartTag = "/*";
	private static String comment = "--";
	private static String javaComment = "//";
	private static String commentEnd = "*/";
	private static String commentBlankEnd = "* /";

	public SqlParser() throws MessageHandlerException
	{
		JAXBContext jc;
		try
		{
			// TODO Hier gibt es einen Bug. Man kann auch
			// anderen Klassen außer ParserSchema
			// übergeben.
			jc = JAXBContext.newInstance(ParserSchema.class);
			Unmarshaller u = jc.createUnmarshaller();
			parserSchema = (ParserSchema) u.unmarshal(SqlParser.class.getResourceAsStream("/SQLParser.xml"));
		}
		catch (JAXBException e)
		{
			RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.ERROR, "JAXB Exception occurred!", e.getMessage());
			throw new MessageHandlerException(e);
		}
	}

	public void parse(SqlStatementList statementList) throws MessageHandlerException //NOSONAR Splitting the algorithm would not be handy
	{
		boolean run = true;
		boolean statementFinished = false;
		String originalLine = null;
		int index = 0;
		BufferedReader reader = null;

		try
		{
			reader = statementList.getFileReader();

			boolean isInCommentModeOutsideStatement = false;
			StringBuilder commentBuilder = new StringBuilder();

			while ((originalLine = reader.readLine()) != null) //NOSONAR Assignment in operand is valid behaviour
			{
				String tempLine = originalLine;
				String matchingLine = tempLine.toLowerCase(Locale.ENGLISH).trim();

				if (isInCommentModeOutsideStatement)
				{
					commentBuilder.append("\n").append(originalLine);

					if (isLineEndingComment(matchingLine))
					{
						AtomicSqlStatement stmt = new AtomicSqlStatement(statementList, commentBuilder.toString(), true);
						stmt.setStatementIndexFrom(index + 1);
						index += commentBuilder.length();
						stmt.setStatementIndexTo(index);
						isInCommentModeOutsideStatement = false;
					}

					continue;
				}

				if (isLineStartingComment(matchingLine))
				{
					commentBuilder = new StringBuilder(originalLine);
					isInCommentModeOutsideStatement = true;
					continue;
				}

				if (isSingleLineCommentOutsideStatement(matchingLine))
				{
					AtomicSqlStatement stmt = new AtomicSqlStatement(statementList, matchingLine, true);
					stmt.setStatementIndexFrom(index + 1);
					index += commentBuilder.length();
					stmt.setStatementIndexTo(index);

					continue;
				}

				ParserDefinitionDocument parseDefinitionDocument = parserSchema.getParserDefinitionDocument();
				List<StartTag> startTagList = parseDefinitionDocument.getStartTag();
				Iterator<StartTag> iter = startTagList.iterator();

				while (iter.hasNext())
				{
					statementFinished = false;
					StringBuffer source = new StringBuffer();
					run = true;

					StartTag listElement = iter.next();
					String startTag = listElement.getTag().toLowerCase().trim();
					List<EndTag> endTagList = listElement.getEndTag();

					if (matchingLine.startsWith(startTag) || matchingLine.endsWith(startTag))
					{
						boolean isInCommentModeInsideStatement = false;
						while (run)
						{
							if (!isInCommentModeInsideStatement)
							{
								for (EndTag endElement : endTagList)
								{
									String endTag = endElement.getTag().trim();

									if (isCommentLineBegin(matchingLine, endTag))
									{
										run = false;
										break;
									}
									else if (isCommentLineEnd(matchingLine, endTag))
									{
										run = false;
										source.append(StringUtils.removeEnd(originalLine, endTag));
										statementFinished = true;
										break;
									}
								}
								if (this.isLineStartingComment(matchingLine))
								{
									isInCommentModeInsideStatement = true;
								}
							}
							else
							{
								if (this.isLineEndingComment(matchingLine))
								{
									isInCommentModeInsideStatement = false;
								}
							}

							if (run)
							{
								source.append(originalLine + " " + "\n");

								String lineBuffer = reader.readLine();

								if (lineBuffer != null && !";".equals(lineBuffer.trim()))
								{
									originalLine = lineBuffer;
									matchingLine = lineBuffer.toLowerCase(Locale.ENGLISH).trim();
								}
								else
								{
									run = false;
									statementFinished = true;
								}
							}
						}

						String s = StringUtils.removeEnd(source.toString(), "\n");
						AtomicSqlStatement atomicSqlStatement = new AtomicSqlStatement(statementList, s);
						atomicSqlStatement.setStatementIndexFrom(index + 1);

						index += atomicSqlStatement.getLength();

						atomicSqlStatement.setStatementIndexTo(index);

						break;
					}
				}
				if (!statementFinished)
				{
					AtomicSqlStatement atomicSqlStatement = new AtomicSqlStatement(statementList, originalLine);
					atomicSqlStatement.setStatementIndexFrom(index + 1);

					index += atomicSqlStatement.getLength();

					atomicSqlStatement.setStatementIndexTo(index);
				}
			}
		}
		catch (IOException e)
		{
			RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.ERROR, "Error while parsing SQL file!", e.getMessage());
			throw new MessageHandlerException(e);
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					RemindContext.getInstance().getMessageHandler().addMessage(MessageLevel.WARNING, "BufferedReader in SQLParser class was not closed!", e.getMessage());
				}
			}
		}
	}

	private boolean isSingleLineCommentOutsideStatement(String compareLine)
	{
	    String tmpLine = compareLine.trim();
	    //single line comment 
	    if (tmpLine.startsWith(comment))
	    {
	        return true;
	    }
	    //multi line comment written as single line comment.
	    if (tmpLine.startsWith(commentStartTag) && (tmpLine.endsWith(commentEnd) || tmpLine.endsWith(commentBlankEnd)))
	    {
	        return true;
	    }
	    
	    return false;
	}

	private boolean isCommentLineBegin(String compareLine, String tag)
	{
		String tmpLine = compareLine.trim();

		// Java commands consist of one whole command including java code and java comments, so we have to exclude java comments from recognition.
		// Otherwise the parser would think the statements ends here.
		return (tmpLine.startsWith(tag) && //NOSONAR we need 4 boolean checks
				!(tmpLine.startsWith(javaComment) || 
				        tmpLine.startsWith(commentStartTag) || 
				        tmpLine.endsWith(commentBlankEnd) || 
				        tmpLine.endsWith(commentEnd))); 

	}

	private boolean isCommentLineEnd(String compareLine, String tag)
	{
		return (compareLine.endsWith(tag) &&  //NOSONAR we need 5 boolean checks
				!(compareLine.startsWith(javaComment) || 
				        compareLine.startsWith(commentStartTag) || 
				        compareLine.endsWith(commentBlankEnd) || 
				        compareLine.endsWith(commentEnd) || 
				        compareLine.startsWith(comment))
			 );
	}

	private boolean isLineStartingComment(String compareLine)
	{
		if (compareLine.contains(commentStartTag))
		{
			String temp = compareLine.substring(compareLine.lastIndexOf(commentStartTag) + commentStartTag.length());

			if (!(temp.contains(commentEnd)))
			{
				return true;
			}
		}

		return false;
	}

	private boolean isLineEndingComment(String compareLine)
	{
		if (compareLine.contains(commentEnd))
		{
			String temp = compareLine.substring(compareLine.lastIndexOf(commentEnd) + commentEnd.length());

			if (!(temp.contains(commentStartTag)))
			{
				return true;
			}
		}

		return false;
	}
}
