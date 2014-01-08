package at.jit.remind.web.rest.dto;

import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DurationFormatUtils;

public class ServerStatusDto
{
	private String vmName;
	private String vmVersion;
	private String vmVendor;

	private String startTime;
	private String uptime;

	private long heapInit;
	private long heapUsed;
	private long heapMax;

	private int threadCount;
	private int threadPeakCount;

	private String systemArch;
	private String systemName;
	private double systemLoad;
	private int availableProcessors;

	public String getVmName()
	{
		return vmName;
	}

	public void setVmName(String vmName)
	{
		this.vmName = vmName;
	}

	public String getVmVersion()
	{
		return vmVersion;
	}

	public void setVmVersion(String vmVersion)
	{
		this.vmVersion = vmVersion;
	}

	public String getVmVendor()
	{
		return vmVendor;
	}

	public void setVmVendor(String vmVendor)
	{
		this.vmVendor = vmVendor;
	}

	public String getStartTime()
	{
		return startTime;
	}

	public void setStartTime(Date startTime)
	{
		this.startTime = DateFormatUtils.format(startTime, "dd.MM.yyyy HH:mm:ss");
	}

	public String getUptime()
	{
		return uptime;
	}

	public void setUptime(long uptime)
	{
		this.uptime = DurationFormatUtils.formatDuration(uptime, "H:mm:ss", true);
	}

	public long getHeapInit()
	{
		return heapInit;
	}

	public void setHeapInit(long heapInit)
	{
		this.heapInit = heapInit;
	}

	public long getHeapUsed()
	{
		return heapUsed;
	}

	public void setHeapUsed(long heapUsed)
	{
		this.heapUsed = heapUsed;
	}

	public long getHeapMax()
	{
		return heapMax;
	}

	public void setHeapMax(long heapMax)
	{
		this.heapMax = heapMax;
	}

	public int getThreadCount()
	{
		return threadCount;
	}

	public void setThreadCount(int threadCount)
	{
		this.threadCount = threadCount;
	}

	public int getThreadPeakCount()
	{
		return threadPeakCount;
	}

	public void setThreadPeakCount(int threadPeakCount)
	{
		this.threadPeakCount = threadPeakCount;
	}

	public String getSystemArch()
	{
		return systemArch;
	}

	public void setSystemArch(String systemArch)
	{
		this.systemArch = systemArch;
	}

	public String getSystemName()
	{
		return systemName;
	}

	public void setSystemName(String systemName)
	{
		this.systemName = systemName;
	}

	public double getSystemLoad()
	{
		return systemLoad;
	}

	public void setSystemLoad(double systemLoad)
	{
		this.systemLoad = systemLoad;
	}

	public int getAvailableProcessors()
	{
		return availableProcessors;
	}

	public void setAvailableProcessors(int availableProcessors)
	{
		this.availableProcessors = availableProcessors;
	}
}
