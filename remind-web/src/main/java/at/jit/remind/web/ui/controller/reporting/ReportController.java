package at.jit.remind.web.ui.controller.reporting;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.jboss.solder.logging.Logger;

import at.jit.remind.web.domain.base.model.CriteriaQueryParameterHolder;
import at.jit.remind.web.domain.base.model.EqualFilter;
import at.jit.remind.web.domain.base.model.GreaterThanOrEqualFilter;
import at.jit.remind.web.domain.base.model.LessThanOrEqualFilter;
import at.jit.remind.web.domain.base.model.SortOrder;
import at.jit.remind.web.domain.base.model.ToStringLikeFilter;
import at.jit.remind.web.domain.context.reporting.model.PersistedDeploymentInformation;
import at.jit.remind.web.domain.context.reporting.service.PersistedDeploymentService;
import at.jit.remind.web.domain.security.qualifier.LoggedIn;
import at.jit.remind.web.ui.controller.base.EntityListControllerBase;
import at.jit.remind.web.ui.controller.base.EntityListDataModelBase;
import at.jit.remind.web.ui.controller.security.LoginController;

@LoggedIn
@Named
@SessionScoped
public class ReportController extends EntityListControllerBase<PersistedDeploymentInformation>
{
	private static final long serialVersionUID = 8946430057743263431L;

	private static final String developerFilterName = "developer";
	private static final String statusDetailsFilterName = "statusDetails";
	private static final String statusFilterName = "status";
	private static final String targetInfoFilterName = "targetInfo";
	private static final String sourceInfoFilterName = "sourceInfo";
	private static final String createdOnUpperFilterName = "createdOn.upper";
	private static final String createdOnLowerFilterName = "createdOn.lower";
	private static final String testCycleNumberFilterName = "testCycleNumber";
	private static final String deploymentVersionFilterName = "deploymentVersion";
	private static final String targetFilterName = "target";
	private static final String environmentFilterName = "environment";
	private static final String releaseFilterName = "release";

	@Inject
	private Logger logger;

	@Inject
	private LoginController loginController;

	@EJB
	private PersistedDeploymentService deploymentService;

	private ReportDataModel dataModel;

	@PostConstruct
	protected void initialize()
	{
		dataModel = new ReportDataModel();
		dataModel.setEntityService(deploymentService);

		// descending
		ToStringLikeFilter<PersistedDeploymentInformation> toStringLikefilter = new ToStringLikeFilter<PersistedDeploymentInformation>(releaseFilterName);
		toStringLikefilter.switchSortOrder();
		toStringLikefilter.switchSortOrder();
		dataModel.addParameter(releaseFilterName, toStringLikefilter);

		toStringLikefilter = new ToStringLikeFilter<PersistedDeploymentInformation>(environmentFilterName);
		toStringLikefilter.switchSortOrder();
		toStringLikefilter.switchSortOrder();
		dataModel.addParameter(environmentFilterName, toStringLikefilter);

		// ascending
		toStringLikefilter = new ToStringLikeFilter<PersistedDeploymentInformation>(targetFilterName);
		toStringLikefilter.switchSortOrder();
		dataModel.addParameter(targetFilterName, toStringLikefilter);

		// unsorted
		dataModel.addParameter(deploymentVersionFilterName, new ToStringLikeFilter<PersistedDeploymentInformation>(deploymentVersionFilterName));
		dataModel.addParameter(testCycleNumberFilterName, new EqualFilter<PersistedDeploymentInformation, BigInteger>(testCycleNumberFilterName,
				BigInteger.class));

		// pre-defined ordering on createdOn is descending
		dataModel.addParameter(createdOnLowerFilterName, new GreaterThanOrEqualFilter<PersistedDeploymentInformation, Date>("createdOn", Date.class));
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();
		dataModel.addParameter(createdOnUpperFilterName, new LessThanOrEqualFilter<PersistedDeploymentInformation, Date>("createdOn", Date.class));

		// unsorted
		dataModel.addParameter(sourceInfoFilterName, new ToStringLikeFilter<PersistedDeploymentInformation>(sourceInfoFilterName));
		dataModel.addParameter(targetInfoFilterName, new ToStringLikeFilter<PersistedDeploymentInformation>(targetInfoFilterName));
		dataModel.addParameter(statusFilterName, new ToStringLikeFilter<PersistedDeploymentInformation>(statusFilterName));
		dataModel.addParameter(statusDetailsFilterName, new ToStringLikeFilter<PersistedDeploymentInformation>(statusDetailsFilterName));
		dataModel.addParameter(developerFilterName, new ToStringLikeFilter<PersistedDeploymentInformation>(developerFilterName));
	}

	@Override
	public ReportDataModel getDataModel()
	{
		return dataModel;
	}

	public void resetFilters()
	{
		dataModel.getParameter(releaseFilterName).clear();
		dataModel.getParameter(releaseFilterName).switchSortOrder();
		dataModel.getParameter(releaseFilterName).switchSortOrder();

		dataModel.getParameter(environmentFilterName).clear();
		dataModel.getParameter(environmentFilterName).switchSortOrder();
		dataModel.getParameter(environmentFilterName).switchSortOrder();

		dataModel.getParameter(targetFilterName).clear();
		dataModel.getParameter(targetFilterName).switchSortOrder();

		dataModel.getParameter(deploymentVersionFilterName).clear();
		dataModel.getParameter(testCycleNumberFilterName).clear();

		// pre-defined ordering on createdOn is descending
		dataModel.getParameter(createdOnLowerFilterName).clear();
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();

		dataModel.getParameter(createdOnUpperFilterName).clear();
		dataModel.getParameter(sourceInfoFilterName).clear();
		dataModel.getParameter(targetInfoFilterName).clear();
		dataModel.getParameter(statusFilterName).clear();
		dataModel.getParameter(statusDetailsFilterName).clear();
		dataModel.getParameter(developerFilterName).clear();
	}

	// TODO: when dataModel is retrieved via getDataModel() method, securty interceptor does not fire. Check if
	// beans.xml is missing in jee-jsf-base project.
	public ReportDataModel getModel()
	{
		return getDataModel();
	}

	public void download()
	{
		logger.info("reportController.download() called");

		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) faces.getExternalContext().getResponse();
		response.setContentType("application/force-download");
		response.setHeader("Content-disposition", "inline; filename=\"DeploymentReport-" + new Date() + ".csv\"");

		try
		{
			ServletOutputStream os = response.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);

			osw.append("RemInD Deployment Report:\n").append("Generated at ").append(new SimpleDateFormat().format(new Date())).append(" by user ")
					.append(loginController.getSessionUser().getUsername()).append("\n\n\n");

			dataModel.exportHeader(osw);
			dataModel.exportData(osw);

			osw.append("\n\nRemInD - Remote Interactive Deployment - Powered by J-IT\n\n");
			osw.flush();
		}
		catch (IOException e)
		{
			// TODO: handle exception
		}

		faces.responseComplete();
	}

	private static final class ReportDataModel extends EntityListDataModelBase<PersistedDeploymentInformation>
	{
		private ReportDataModel()
		{
			super(new CriteriaQueryParameterHolder<PersistedDeploymentInformation>(PersistedDeploymentInformation.class));
		}

		public void exportHeader(Writer writer) throws IOException
		{
			writer.append("Filter values: ");

			// TODO: think about moving this implementation to base class
			for (String key : getParameterHolder().getOrderedParameterList())
			{
				if (getParameter(key).getValue() != null)
				{
					writer.append('[').append(key).append(' ').append(getParameter(key).getValue().toString()).append("] ");
				}
			}
			writer.append('\n');

			writer.append("Sort orders: ");
			for (String key : getParameterHolder().getOrderedParameterList())
			{
				if (!SortOrder.Unsorted.equals(getParameter(key).getSortOrder()))
				{
					writer.append('[').append(key).append(' ').append(getParameter(key).getSortOrder().toString()).append("] ");
				}
			}
			writer.append("\n\n");

			writer.append("Release").append(';').append("Environment").append(';').append("Target").append(';').append("Deployment Version").append(';')
					.append("Test Cycle Number").append(';').append("Created On").append(';').append("Source Info").append(';').append("Target Info")
					.append(';').append("Developer").append("\n\n");
		}

		public void exportData(Writer writer) throws IOException
		{
			int pageSize = 50;
			for (int i = 0; i < getRowCount(); i += pageSize)
			{
				List<PersistedDeploymentInformation> results = loadData(i, pageSize);
				if (results.size() == 0)
				{
					break;
				}

				for (PersistedDeploymentInformation dpi : results)
				{
					writer.append(dpi.getRelease()).append(';').append(dpi.getEnvironment()).append(';').append(dpi.getTarget()).append(';')
							.append(dpi.getDeploymentVersion()).append(';').append(dpi.getTestCycleNumber().toString()).append(';')
							.append(dpi.getCreatedOn().toString()).append(';').append(dpi.getSourceInfo()).append(';').append(dpi.getTargetInfo()).append(';')
							.append(dpi.getDeveloper()).append('\n');
				}
			}
		}
	}
}
