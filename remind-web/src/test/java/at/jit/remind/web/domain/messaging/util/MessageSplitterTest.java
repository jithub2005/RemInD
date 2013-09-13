package at.jit.remind.web.domain.messaging.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class MessageSplitterTest
{
	@Test
	public void testMessageSpliterWithUsualMessage()
	{
		String message = "Executing statement: ALTER TABLE `test` ADD COLUMN `first_name` VARCHAR(256) NULL, ADD COLUMN `last_name` VARCHAR(256) NULL AFTER `first_name`, ADD COLUMN `username` VARCHAR(256) NULL AFTER `last_name`, ADD COLUMN `password` VARCHAR(124) NULL AFTER `username`";

		List<String> listOfSubstrings = MessageSplitter.splitMessageByLengthAndSpace(message, 50);

		assertSame("Expected number of new message substrings is 6", 6, listOfSubstrings.size());

		for (String messageSubstring : listOfSubstrings)
		{
			assertTrue("Message substring length is never bigger than maximum number of characters.", messageSubstring.length() <= 50);
		}

		assertEquals("Expecteding first substring from list of message substrings.", "Executing statement: ALTER TABLE `test` ADD ", listOfSubstrings.get(0));
		assertEquals("Expecteding second substring from list of message substrings.", "COLUMN `first_name` VARCHAR(256) NULL, ADD COLUMN ",
				listOfSubstrings.get(1));
		assertEquals("Expecteding third substring from list of message substrings.", "`last_name` VARCHAR(256) NULL AFTER `first_name`, ",
				listOfSubstrings.get(2));
		assertEquals("Expecteding fourth substring from list of message substrings.", "ADD COLUMN `username` VARCHAR(256) NULL AFTER ", listOfSubstrings.get(3));
		assertEquals("Expecteding fifth substring from list of message substrings.", "`last_name`, ADD COLUMN `password` VARCHAR(124) ",
				listOfSubstrings.get(4));
		assertEquals("Expecteding sixth substring from list of message substrings.", "NULL AFTER `username`", listOfSubstrings.get(5));
	}

	@Test
	public void testMessageSpliterWithUnusualMessage()
	{
		String message = "Retrieving data from SVN: repositoryUrl=http://dev.jit.at/svn/remind-testing/long/files/herarchy/tree/, path=trunk/Rel 13.03/dbFiles/long/files/herarchy/tree/alter-table-test.sql, revision=28, repositoryRevision=28";

		List<String> listOfSubstrings = MessageSplitter.splitMessageByLengthAndSpace(message, 50);

		assertSame("Expected number of new message substrings is 5", 5, listOfSubstrings.size());

		for (String messageSubstring : listOfSubstrings)
		{
			assertTrue("Message substring length is never bigger than maximum number of characters.", messageSubstring.length() <= 50);
		}

		assertEquals("Expecteding first substring from list of message substrings.", "Retrieving data from SVN: ", listOfSubstrings.get(0));
		assertEquals("Expecteding second substring from list of message substrings.", "repositoryUrl=http://dev.jit.at/svn/remind-testing",
				listOfSubstrings.get(1));
		assertEquals("Expecteding third substring from list of message substrings.", "/long/files/herarchy/tree/, path=trunk/Rel ", listOfSubstrings.get(2));
		assertEquals("Expecteding fourth substring from list of message substrings.", "13.03/dbFiles/long/files/herarchy/tree/alter-table",
				listOfSubstrings.get(3));
		assertEquals("Expecteding fifth substring from list of message substrings.", "-test.sql, revision=28, repositoryRevision=28", listOfSubstrings.get(4));
	}

}
