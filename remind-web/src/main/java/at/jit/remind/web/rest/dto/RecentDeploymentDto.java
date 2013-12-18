package at.jit.remind.web.rest.dto;

import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;

public class RecentDeploymentDto extends DtoBase
{
	private Long fileInfoId;
	private String date;
	private String fileName;

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
}
