package at.jit.remind.core.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;

import at.jit.remind.core.xml.Change;
import at.jit.remind.core.xml.Environment;

public class EnvironmentTestCycleTest
{
	@Test
	public void canHandleExistingEnvironment() throws Exception
	{
		assertTrue(prepareTest("DEV", 1, 5, "3"));
	}

	@Test
	public void canHandleNonExistingEnvironment() throws Exception
	{
		assertFalse(prepareTest("QM", 1, 5, "3"));
	}

	@Test
	public void doesNotIgnoreCaseInsensitiveEnvironment() throws Exception
	{
		assertFalse(prepareTest("deV", 1, 5, "3"));
		assertFalse(prepareTest("proDuCtIon", 1, 5, "3"));
	}

	@Test
	public void testCycleNumberInLowestPossibleRangeAchievesSuccess() throws Exception
	{
		assertTrue(prepareTest("DEV", 3, 6, "3"));
	}

	@Test
	public void testCycleNumberInHighestPossibleRangeAchievesSuccess() throws Exception
	{
		assertTrue(prepareTest("DEV", 3, 6, "6"));
	}

	@Test
	public void testCycleNumberLowerThanRangeCausesReject() throws Exception
	{
		assertFalse(prepareTest("DEV", 3, 6, "2"));
	}

	@Test
	public void testCycleNumberHigherThanRangeCausesReject() throws Exception
	{
		assertFalse(prepareTest("DEV", 3, 6, "7"));
	}

	@Test
	public void canHandleInvalidEnvironments() throws Exception
	{
		assertFalse(prepareTest("FOO", 3, 6, "7"));
	}

	private boolean prepareTest(String env, int tcLower, int tcUpper, String changeTcNmbr) throws Exception
	{
		Change change = new Change();
		change.getEnvironment().add(Environment.DEV);
		change.getEnvironment().add(Environment.PRODUCTION);
		change.setTestCycleNumber(new BigInteger(changeTcNmbr));

		ChangeModel changeModel = new ChangeModel();
		changeModel.setElement(change);

		UserInput userInput = new UserInput();
		userInput.setEnvironment(env);
		userInput.setLowerTestCycleNumber(tcLower);
		userInput.setUpperTestCycleNumber(tcUpper);

		return changeModel.appliesFor(userInput);
	}
}
