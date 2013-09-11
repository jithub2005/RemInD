package at.jit.remind.core.model;

public class UserInput
{
	private int lowerTestCycleNumber;
	private int upperTestCycleNumber;

	private String environment;

	public UserInput()
	{
		this(Integer.MIN_VALUE, Integer.MAX_VALUE, "");
	}

	public UserInput(int lowerTestCycleNumber, int upperTestCycleNumber, String environment)
	{
		this.lowerTestCycleNumber = lowerTestCycleNumber;
		this.upperTestCycleNumber = upperTestCycleNumber;
		this.environment = environment;
	}

	public int getLowerTestCycleNumber()
	{
		return lowerTestCycleNumber;
	}

	public void setLowerTestCycleNumber(int lowerTestCycleNumber)
	{
		this.lowerTestCycleNumber = lowerTestCycleNumber;
	}

	public int getUpperTestCycleNumber()
	{
		return upperTestCycleNumber;
	}

	public void setUpperTestCycleNumber(int upperTestCycleNumber)
	{
		this.upperTestCycleNumber = upperTestCycleNumber;
	}

	public String getEnvironment()
	{
		return environment;
	}

	public void setEnvironment(String environment)
	{
		this.environment = environment;
	}
}
