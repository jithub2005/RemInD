package at.jit.remind.web.rest;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import at.jit.remind.web.rest.dto.ServerStatusDto;

@Path("/server")
public class ServerStatusRestService
{
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/status")
	public ServerStatusDto getServerStatus()
	{
		ServerStatusDto statusDto = new ServerStatusDto();		
		
		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
		
		statusDto.setVmName(runtimeBean.getVmName());
		statusDto.setVmVendor(runtimeBean.getVmVendor());
		statusDto.setVmVersion(runtimeBean.getVmVersion());
		statusDto.setStartTime(new Date(runtimeBean.getStartTime()));
		statusDto.setUptime(runtimeBean.getUptime());

		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();

		statusDto.setHeapInit(heapUsage.getInit());
		statusDto.setHeapUsed(heapUsage.getUsed());
		statusDto.setHeapMax(heapUsage.getMax());
		
		ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
		statusDto.setThreadCount(threadBean.getThreadCount());
		statusDto.setThreadPeakCount(threadBean.getPeakThreadCount());

		OperatingSystemMXBean operatingSystemBean = ManagementFactory.getOperatingSystemMXBean();
		statusDto.setSystemArch(operatingSystemBean.getArch());
		statusDto.setSystemName(operatingSystemBean.getName());
		statusDto.setSystemLoad(operatingSystemBean.getSystemLoadAverage());
		statusDto.setAvailableProcessors(operatingSystemBean.getAvailableProcessors());
		
		return statusDto;
	}
}
