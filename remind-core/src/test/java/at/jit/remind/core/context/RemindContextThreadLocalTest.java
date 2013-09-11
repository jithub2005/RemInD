package at.jit.remind.core.context;

import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;

import junit.framework.Assert;

import org.junit.Test;

import at.jit.remind.core.context.messaging.ListBasedMessageHandler;
import at.jit.remind.core.context.messaging.MessageHandler;

public class RemindContextThreadLocalTest
{
	private CountDownLatch threadFinished = new CountDownLatch(2);

	private MessageHandler messageHandlerThread1 = new ListBasedMessageHandler();
	private MessageHandler messageHandlerThread2 = new ListBasedMessageHandler();

	private CountDownLatch threadsHaveSetMessageHandler = new CountDownLatch(2);
	private CountDownLatch threadsHaveGotMessageHandler = new CountDownLatch(2);

	class TestThread extends Thread
	{
		private MessageHandler messageHandler;
		private MessageHandler retrievedMessageHandler;

		public TestThread(String threadname, MessageHandler messageHandler)
		{
			super(threadname);
			this.messageHandler = messageHandler;
		}

		public synchronized MessageHandler getMessageHandler()
		{
			return messageHandler;
		}

		public synchronized MessageHandler getRetrievedMessageHandler()
		{
			return retrievedMessageHandler;
		}

		@Override
		public void run()
		{
			try
			{
				RemindContext.getInstance().setMessageHandler(messageHandler);
				threadsHaveSetMessageHandler.countDown();
				threadsHaveSetMessageHandler.await();

				retrievedMessageHandler = RemindContext.getInstance().getMessageHandler();
				threadsHaveGotMessageHandler.countDown();
				threadsHaveGotMessageHandler.await();
			}
			catch (InterruptedException e)
			{
				fail(e.getMessage());
			}

			threadFinished.countDown();
		}
	}

	@Test
	public void canHandleDifferentUserInstances() throws InterruptedException
	{
		TestThread thread1 = new TestThread("Thread1", messageHandlerThread1);
		TestThread thread2 = new TestThread("Thread2", messageHandlerThread2);
		thread1.start();
		thread2.start();

		threadFinished.await();

		Assert.assertEquals(thread1.getMessageHandler(), thread1.getRetrievedMessageHandler());
		Assert.assertEquals(thread2.getMessageHandler(), thread2.getRetrievedMessageHandler());

		Assert.assertNotSame(thread1.getRetrievedMessageHandler(), thread2.getRetrievedMessageHandler());

	}
}
