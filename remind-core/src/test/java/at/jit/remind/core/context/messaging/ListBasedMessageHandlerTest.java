package at.jit.remind.core.context.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import at.jit.remind.core.context.messaging.MessageHandler.MessageLevel;
import at.jit.remind.core.context.messaging.MessageHandler.Range;

public class ListBasedMessageHandlerTest
{
	@Test
	public void rangeRecognizesMessageWithSpecifiedMessageLevel() throws Exception
	{
		ListBasedMessageHandler messageHandler = new ListBasedMessageHandler();

		Range range = messageHandler.openRange();
		messageHandler.addMessage("Message 1");
		messageHandler.addMessage("Message 2");
		messageHandler.addMessage("Message 3");
		messageHandler.addMessage(MessageLevel.WARNING, "Message 4 with warning", "Warning occured in message 4");
		messageHandler.addMessage("Message 5");
		messageHandler.addMessage("Message 6");
		messageHandler.addMessage(MessageLevel.WARNING, "Message 7 with warning", "Warning occured in message 7");
		messageHandler.addMessage(MessageLevel.ERROR, "Message 8 with error", "Error occured in message 8");
		messageHandler.addMessage("Message 9");
		messageHandler.addMessage("Message 10");
		messageHandler.addMessage("Message 11");
		range.close();

		assertTrue(range.containsMessageWithLevel(MessageLevel.ERROR));
	}

	@Test
	public void returnedMessageListEqualsOriginalList()
	{
		ListBasedMessageHandler messageHandler = new ListBasedMessageHandler();

		Range range = messageHandler.openRange();
		messageHandler.addMessage("Message 1");
		messageHandler.addMessage("Message 2");
		messageHandler.addMessage("Message 3");
		messageHandler.addMessage(MessageLevel.ERROR, "Message 4 with error", "Error occured in message 4");
		messageHandler.addMessage("Message 5");
		messageHandler.addMessage("Message 6");
		messageHandler.addMessage(MessageLevel.WARNING, "Message 7 with warning", "Warning occured in message 7");
		messageHandler.addMessage("Message 8");
		messageHandler.addMessage("Message 9");
		messageHandler.addMessage("Message 10");
		messageHandler.addMessage("Message 11");
		range.close();

		assertTrue(messageHandler.getMessageList().equals(range.getMessages()));
	}

	@Test
	public void rangeCanHandleEmptyList()
	{
		ListBasedMessageHandler messageHandler = new ListBasedMessageHandler();
		Range range = messageHandler.openRange();
		range.close();

		assertTrue(range.getMessages().isEmpty());
	}

	@Test
	public void rangeSizeCalculatedProberly()
	{
		ListBasedMessageHandler messageHandler = new ListBasedMessageHandler();

		messageHandler.addMessage("Message 1");

		Range range = messageHandler.openRange();
		messageHandler.addMessage("Message 2");
		messageHandler.addMessage(MessageLevel.WARNING, "Message 3 with warning", "Warning occured in message 3");
		messageHandler.addMessage("Message 4");
		messageHandler.addMessage("Message 5");
		range.close();

		messageHandler.addMessage("Message 6");
		messageHandler.addMessage("Message 7");

		assertEquals(range.getMessages().size(), 4);
	}

	@Test
	public void rangeRecognizesUnclosedRanges()
	{
		ListBasedMessageHandler messageHandler = new ListBasedMessageHandler();

		Range range = messageHandler.openRange();
		messageHandler.addMessage("Message 1");
		messageHandler.addMessage("Message 2");
		messageHandler.addMessage("Message 3");
		messageHandler.addMessage(MessageLevel.ERROR, "Message 4 with error", "Error occured in message 4");
		messageHandler.addMessage("Message 5");

		assertTrue(!range.isClosed());
	}

	@Test
	public void multipleRangesPossible()
	{
		ListBasedMessageHandler messageHandler = new ListBasedMessageHandler();

		Range installationDocument = messageHandler.openRange();
		Range installationBlock1 = messageHandler.openRange();

		Range range1 = messageHandler.openRange();
		messageHandler.addMessage("Message 1");
		messageHandler.addMessage("Message 2");
		messageHandler.addMessage("Message 3");
		messageHandler.addMessage(MessageLevel.WARNING, "Message 4 with warning", "Warning occured in message 4");
		messageHandler.addMessage("Message 5");
		range1.close();

		Range range2 = messageHandler.openRange();
		messageHandler.addMessage("Message 6");
		messageHandler.addMessage(MessageLevel.WARNING, "Message 7 with warning", "Warning occured in message 7");
		messageHandler.addMessage(MessageLevel.ERROR, "Message 8 with error", "Error occured in message 8");
		messageHandler.addMessage("Message 9");
		messageHandler.addMessage("Message 10");
		range2.close();

		installationBlock1.close();

		Range installationBlock2 = messageHandler.openRange();
		Range range3 = messageHandler.openRange();
		messageHandler.addMessage("Message 11");
		messageHandler.addMessage("Message 12");
		messageHandler.addMessage("Message 13");
		messageHandler.addMessage("Message 14");
		range3.close();

		Range range4 = messageHandler.openRange();
		messageHandler.addMessage("Message 15");
		messageHandler.addMessage("Message 16");
		messageHandler.addMessage("Message 17");
		messageHandler.addMessage("Message 18");
		messageHandler.addMessage("Message 19");
		messageHandler.addMessage("Message 20");
		messageHandler.addMessage("Message 21");
		messageHandler.addMessage("Message 22");
		range4.close();

		Range range5 = messageHandler.openRange();
		messageHandler.addMessage("Message 23");
		messageHandler.addMessage("Message 24");
		messageHandler.addMessage("Message 25");
		messageHandler.addMessage("Message 26");
		messageHandler.addMessage("Message 27");
		messageHandler.addMessage("Message 28");
		messageHandler.addMessage("Message 29");
		messageHandler.addMessage("Message 30");
		range5.close();

		installationBlock2.close();
		installationDocument.close();

		assertEquals(installationDocument.getMessages().size(), 30);
		assertEquals(installationBlock1.getMessages().size(), 10);
		assertEquals(installationBlock2.getMessages().size(), 20);

		assertEquals(range1.getMessages().size(), 5);
		assertEquals(range2.getMessages().size(), 5);
		assertEquals(range3.getMessages().size(), 4);
		assertEquals(range4.getMessages().size(), 8);
		assertEquals(range5.getMessages().size(), 8);

		assertTrue(range1.containsMessageWithLevel(MessageLevel.WARNING));
		assertTrue(range2.containsMessageWithLevel(MessageLevel.ERROR));
	}

	@Test
	public void overlappingRangesPossible()
	{
		ListBasedMessageHandler messageHandler = new ListBasedMessageHandler();

		Range range1 = messageHandler.openRange();
		messageHandler.addMessage("Message 1");
		messageHandler.addMessage("Message 2");
		messageHandler.addMessage("Message 3");
		messageHandler.addMessage("Message 4");
		messageHandler.addMessage("Message 5");
		messageHandler.addMessage("Message 6");

		Range range2 = messageHandler.openRange();
		messageHandler.addMessage("Message 7");
		messageHandler.addMessage("Message 8");
		messageHandler.addMessage("Message 9");
		messageHandler.addMessage("Message 10");
		messageHandler.addMessage("Message 11");
		messageHandler.addMessage("Message 12");
		range1.close();

		messageHandler.addMessage("Message 13");
		messageHandler.addMessage("Message 14");
		messageHandler.addMessage("Message 15");
		messageHandler.addMessage("Message 16");
		messageHandler.addMessage("Message 17");
		messageHandler.addMessage("Message 18");
		messageHandler.addMessage("Message 19");
		messageHandler.addMessage("Message 20");
		range2.close();

		assertEquals(range1.getMessages().size(), 12);
		assertEquals(range2.getMessages().size(), 14);

		assertTrue(range1.getMessages().equals(messageHandler.getMessageList().subList(0, 12)));
		assertTrue(range2.getMessages().equals(messageHandler.getMessageList().subList(6, 20)));
	}
}
