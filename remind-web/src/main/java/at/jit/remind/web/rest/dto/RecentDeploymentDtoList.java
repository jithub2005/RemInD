package at.jit.remind.web.rest.dto;

import java.util.ArrayList;

public class RecentDeploymentDtoList extends DtoListBase<RecentDeploymentDto>
{
	public RecentDeploymentDtoList()
	{
		setData(new ArrayList<RecentDeploymentDto>());
	}
}
