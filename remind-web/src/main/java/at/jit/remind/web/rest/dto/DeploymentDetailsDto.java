package at.jit.remind.web.rest.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateFormatUtils;

public class DeploymentDetailsDto
{
	private String date;
	private String fileName;

	private List<StatisticsDto> statistics = new ArrayList<DeploymentDetailsDto.StatisticsDto>();

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
		return statistics;
	}
	
	public StatisticsDto createAndAddStatistics(String environment)
	{
		StatisticsDto dto = new StatisticsDto();
		dto.setEnvironment(environment);
		statistics.add(dto);
		
		return dto;
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

		public void setCountChanges(int countChanges)
		{
			this.countChanges = countChanges;
		}

		public int getCountOk()
		{
			return countOk;
		}

		public void setCountOk(int countOk)
		{
			this.countOk = countOk;
		}

		public int getCountWarning()
		{
			return countWarning;
		}

		public void setCountWarning(int countWarning)
		{
			this.countWarning = countWarning;
		}

		public int getCountError()
		{
			return countError;
		}

		public void setCountError(int countError)
		{
			this.countError = countError;
		}
	}
}
