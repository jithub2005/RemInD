package at.jit.remind.web.rest.dto;

import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;

public class RecentDeploymentDto
{
	private Long fileInfoId;
	private String date;
	private String fileName;

	private String details;

	public Long getFileInfoId()
	{
		return fileInfoId;
	}

	public void setFileInfoId(Long fileInfoId)
	{
		this.fileInfoId = fileInfoId;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = DateFormatUtils.format(date, "dd.MM.yyyy hh:mm:ss");
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public String getDetails()
	{
		return details;
	}

	public void setDetails(String details)
	{
		this.details = details;
	}
}
