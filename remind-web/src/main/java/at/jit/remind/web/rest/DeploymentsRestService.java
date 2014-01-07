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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import at.jit.remind.web.domain.base.model.CriteriaFilterBase;
import at.jit.remind.web.domain.base.model.CriteriaQueryParameterHolder;
import at.jit.remind.web.domain.base.model.GreaterThanOrEqualFilter;
import at.jit.remind.web.domain.base.model.NamedQueryParameterHolder;
import at.jit.remind.web.domain.context.reporting.model.FileInfo;
import at.jit.remind.web.domain.context.reporting.model.PersistedDeploymentInformation;
import at.jit.remind.web.domain.context.reporting.service.FileInfoService;
import at.jit.remind.web.domain.context.reporting.service.PersistedDeploymentService;
import at.jit.remind.web.rest.dto.DeploymentDetailsDto;
import at.jit.remind.web.rest.dto.DeploymentDetailsDto.StatisticsDto;
import at.jit.remind.web.rest.dto.RecentDeploymentDto;
import at.jit.remind.web.rest.dto.RecentDeploymentDtoList;

@Path("/deployments")
public class DeploymentsRestService
{
	private static final String firstResultQueryParameter = "fr";
	private static final String maxResultsQueryParameter = "mr";

	private static final String fileInfoIdQueryParameter = "fiid";

	@Context
	private UriInfo uriInfo;

	@Context
	private Request request;

	@EJB
	private FileInfoService fileInfoService;

	@EJB
	private PersistedDeploymentService deploymentService;

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
			dto.setDetails(createDetailsUrl(fileInfo.getId()));

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

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/details")
	public Response getDeploymentDetails(@DefaultValue("0") @QueryParam(fileInfoIdQueryParameter) long fileInfoId)
	{
		FileInfo fileInfo = fileInfoService.find(fileInfoId);

		if (fileInfo == null)
		{
			return Response.noContent().build();
		}

		DeploymentDetailsDto dto = new DeploymentDetailsDto();
		dto.setFileName(fileInfo.getName());
		dto.setDate(fileInfo.getCreatedOn());

		NamedQueryParameterHolder<PersistedDeploymentInformation> parameterHolder = new NamedQueryParameterHolder<PersistedDeploymentInformation>(
				PersistedDeploymentInformation.class);
		parameterHolder.setQueryName(PersistedDeploymentInformation.FindByFileInfoId);
		parameterHolder.setParameter(PersistedDeploymentInformation.FileInfoId, fileInfoId);
		List<PersistedDeploymentInformation> deploymentInformationList = deploymentService.find(parameterHolder);

		for (PersistedDeploymentInformation pdi : deploymentInformationList)
		{
			StatisticsDto statisticsDto = dto.getStatistics(pdi.getEnvironment());
			statisticsDto.incrementCount(pdi.getStatus());
		}

		return Response.ok(dto).build();
	}

	private String createDetailsUrl(long fileInfoId)
	{
		return uriInfo.getBaseUriBuilder().path("/deployments/details").queryParam(fileInfoIdQueryParameter, fileInfoId).build().toString();
	}
}
