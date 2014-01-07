package at.jit.remind.web.rest.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateFormatUtils;

import at.jit.remind.core.model.status.State;

public class DeploymentDetailsDto
{
	private String date;
	private String fileName;

	private Map<String, StatisticsDto> environmentStatisticsMap = new HashMap<String, DeploymentDetailsDto.StatisticsDto>();

	public String getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = DateFormatUtils.format(date, "dd.MM.yyyy HH:mm:ss");
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public List<StatisticsDto> getStatistics()
	{
		return new ArrayList<DeploymentDetailsDto.StatisticsDto>(environmentStatisticsMap.values());
	}
	
	public StatisticsDto getStatistics(String environment)
	{
		if (!environmentStatisticsMap.containsKey(environment))
		{
			StatisticsDto dto = new StatisticsDto();
			dto.setEnvironment(environment);
			environmentStatisticsMap.put(environment, dto);
			
			return dto;
		}
		
		return environmentStatisticsMap.get(environment);
	}

	public class StatisticsDto
	{
		private String environment;
		private int countChanges;
		private int countOk;
		private int countWarning;
		private int countError;

		public String getEnvironment()
		{
			return environment;
		}

		public void setEnvironment(String environment)
		{
			this.environment = environment;
		}

		public int getCountChanges()
		{
			return countChanges;
		}

		public int getCountOk()
		{
			return countOk;
		}

		public int getCountWarning()
		{
			return countWarning;
		}

		public int getCountError()
		{
			return countError;
		}

		public void incrementCount(String status)
		{
			switch(State.valueOf(status))
			{
				case Ok:
					countOk++;
					countChanges++;
					break;
				case Warning:
					countWarning++;
					countChanges++;
					break;
				case Error:
					countError++;
					countChanges++;
					break;
				case Unknown:
			}
		}
	}
}
