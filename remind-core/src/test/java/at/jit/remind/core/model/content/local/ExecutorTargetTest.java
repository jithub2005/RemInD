package at.jit.remind.core.model.content.local;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import at.jit.remind.core.context.PropertiesFromResourceProvider;
import at.jit.remind.core.context.RemindContext;
import at.jit.remind.core.context.messaging.ListBasedMessageHandler;
import at.jit.remind.core.exception.MessageHandlerException;
import at.jit.remind.core.model.RemindModelFeedback;

public class ExecutorTargetTest
{
	private static final String executableFilePath = "src/test/resources/local/executable.sh";

	private static final String notExecutableFilePath = "src/test/resources/local/notExecutable.sh";

	private static final String environment = "DEV";

	private static ListBasedMessageHandler listBasedMessageHandler = new ListBasedMessageHandler();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		RemindContext.getInstance().setPropertiesProvider(new PropertiesFromResourceProvider());
		RemindContext.getInstance().setMessageHandler(listBasedMessageHandler);

		listBasedMessageHandler.setFeedback(RemindModelFeedback.Abort);
	}

	@After
	public void tearDown() throws IOException
	{
		File file = new File("/tmp/ExecutorTest.txt");
		file.deleteOnExit();
	}

	@Test
	public void canDeploy() throws MessageHandlerException
	{
		ExecutorTarget executorTarget = new ExecutorTarget(environment, ".", "cat");
		executorTarget.deploy(FileUtils.toFile(ExecutorTargetTest.class.getResource("/local/ExecutorTest.txt")));
	}

	@Test
	public void canDeployExecutable() throws MessageHandlerException, IOException
	{
		ExecutorTarget executorTarget = new ExecutorTarget(environment, ".", "sh " + executableFilePath);
		executorTarget.deploy(FileUtils.toFile(ExecutorTargetTest.class.getResource("/local/ExecutorTest.txt")));

		assertTrue("Check if content of two files is same.",
				FileUtils.contentEquals(FileUtils.toFile(ExecutorTargetTest.class.getResource("/local/ExecutorTest.txt")), new File("/tmp/ExecutorTest.txt")));
	}

	@Test(expected = MessageHandlerException.class)
	public void canNotDeploy() throws MessageHandlerException
	{
		ExecutorTarget executorTarget = new ExecutorTarget(environment, "./wrong_path", "sh");
		executorTarget.deploy(FileUtils.toFile(ExecutorTargetTest.class.getResource("/local/ExecutorTest.txt")));
	}

	@Test
	public void validationSuccessful() throws Exception, MessageHandlerException
	{
		ExecutorTarget executorTarget = new ExecutorTarget(environment, ".", "sh " + executableFilePath);
		executorTarget.validate();
	}

	@Test(expected = MessageHandlerException.class)
	public void validationNotSuccessfulFileNotExecutable() throws IOException, MessageHandlerException
	{
		ExecutorTarget executorTarget = new ExecutorTarget(environment, ".", "sh " + notExecutableFilePath);
		executorTarget.validate();
	}

}
