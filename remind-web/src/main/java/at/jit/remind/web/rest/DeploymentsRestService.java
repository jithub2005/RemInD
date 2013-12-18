package at.jit.remind.web.rest;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import at.jit.remind.web.domain.base.model.CriteriaFilterBase;
import at.jit.remind.web.domain.base.model.CriteriaQueryParameterHolder;
import at.jit.remind.web.domain.base.model.GreaterThanOrEqualFilter;
import at.jit.remind.web.domain.context.reporting.model.FileInfo;
import at.jit.remind.web.domain.context.reporting.service.FileInfoService;
import at.jit.remind.web.rest.dto.RecentDeploymentDto;
import at.jit.remind.web.rest.dto.RecentDeploymentDtoList;

@Path("/deployments")
public class DeploymentsRestService
{
	private static final String firstResultQueryParameter = "fr";
	private static final String maxResultsQueryParameter = "mr";

	@Context
	private UriInfo uriInfo;

	@Context
	private Request request;

	@EJB
	private FileInfoService fileInfoService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/recent")
	public RecentDeploymentDtoList getRecentDeplyomentInfos(@DefaultValue("0") @QueryParam(firstResultQueryParameter) int firstResult,
			@DefaultValue("10") @QueryParam(maxResultsQueryParameter) int maxResults)
	{
		CriteriaQueryParameterHolder<FileInfo> parameterHolder = new CriteriaQueryParameterHolder<FileInfo>(FileInfo.class);

		CriteriaFilterBase<FileInfo, Date> createdOnFilter = new GreaterThanOrEqualFilter<FileInfo, Date>("createdOn", Date.class);
		createdOnFilter.switchSortOrder();
		createdOnFilter.switchSortOrder();
		parameterHolder.setParameter("createdOn", createdOnFilter);

		int count = fileInfoService.count(parameterHolder).intValue();
		PaginationHelper paginator = new PaginationHelper(firstResult, count, maxResults);

		parameterHolder.limit(firstResult, maxResults);
		List<FileInfo> fileInfoList = fileInfoService.find(parameterHolder);

		RecentDeploymentDtoList recentDeploymentDtos = new RecentDeploymentDtoList();
		for (FileInfo fileInfo : fileInfoList)
		{
			RecentDeploymentDto dto = new RecentDeploymentDto();
			dto.setFileInfoId(fileInfo.getId());
			dto.setDate(fileInfo.getCreatedOn());
			dto.setFileName(fileInfo.getName());
			recentDeploymentDtos.getData().add(dto);
		}

		recentDeploymentDtos.getPagination().setFirst(createPaginationUrl(paginator.getFirstIndex(), maxResults));
		recentDeploymentDtos.getPagination().setPrevious(createPaginationUrl(paginator.getPreviousIndex(), maxResults));
		recentDeploymentDtos.getPagination().setNext(createPaginationUrl(paginator.getNextIndex(), maxResults));
		recentDeploymentDtos.getPagination().setLast(createPaginationUrl(paginator.getLastIndex(), maxResults));

		return recentDeploymentDtos;
	}

	private String createPaginationUrl(int firstResult, int maxResults)
	{
		return uriInfo.getAbsolutePathBuilder().queryParam(firstResultQueryParameter, firstResult).queryParam(maxResultsQueryParameter, maxResults).build()
				.toString();
	}
}
