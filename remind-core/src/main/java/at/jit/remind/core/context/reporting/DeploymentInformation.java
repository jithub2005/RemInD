package at.jit.remind.core.context.reporting;

import java.math.BigInteger;
import java.util.Set;

public class DeploymentInformation
{
	private String title;
	private String target;
	private String release;
	private String testCycle; // this is the testCycle set in documentInformation part of xml
	private String version;

	private String environment;
	private BigInteger testCycleNumber; // this is the testCycle set in change part of xml

	private String sourceInfo;
	private String targetInfo;
	private String developer;

	private Long actionId;

	private String status;
	private String statusDetails;

	public DeploymentInformation()
	{
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getTarget()
	{
		return target;
	}

	public void setTarget(String target)
	{
		this.target = target;
	}

	public String getRelease()
	{
		return release;
	}

	public void setRelease(String release)
	{
		this.release = release;
	}

	public String getTestCycle()
	{
		return testCycle;
	}

	public void setTestCycle(String testCycle)
	{
		this.testCycle = testCycle;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public String getEnvironment()
	{
		return environment;
	}

	public void setEnvironment(String environment)
	{
		this.environment = environment;
	}

	public BigInteger getTestCycleNumber()
	{
		return testCycleNumber;
	}

	public void setTestCycleNumber(BigInteger testCycleNumber)
	{
		this.testCycleNumber = testCycleNumber;
	}

	public String getSourceInfo()
	{
		return sourceInfo;
	}

	public void setSourceInfo(String sourceInfo)
	{
		this.sourceInfo = sourceInfo;
	}

	public String getTargetInfo()
	{
		return targetInfo;
	}

	public void setTargetInfo(String targetInfo)
	{
		this.targetInfo = targetInfo;
	}

	public String getDeveloper()
	{
		return developer;
	}

	public void setDeveloper(String developer)
	{
		this.developer = developer;
	}

	public Long getActionId()
	{
		return actionId;
	}

	public void setActionId(Long actionId)
	{
		this.actionId = actionId;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getStatusDetails()
	{
		return statusDetails;
	}

	public void setStatusDetails(String statusDetails)
	{
		this.statusDetails = statusDetails;
	}

	public void addStatusDetails(Set<String> details)
	{
		StringBuilder sb = new StringBuilder();
		for (String detail : details)
		{
			sb.append(detail).append('\n');
		}

		statusDetails = sb.toString();
	}
}
