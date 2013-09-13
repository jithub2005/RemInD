package at.jit.remind.web.ui.controller.reporting;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import at.jit.remind.web.domain.base.model.GreaterThanOrEqualFilter;
import at.jit.remind.web.domain.base.model.InCollectionFilter;
import at.jit.remind.web.domain.base.model.LessThanOrEqualFilter;
import at.jit.remind.web.domain.base.model.ToStringLikeFilter;
import at.jit.remind.web.domain.context.reporting.model.ActionLog;
import at.jit.remind.web.domain.context.reporting.service.ActionLogService;
import at.jit.remind.web.domain.context.reporting.service.ActionService;
import at.jit.remind.web.domain.security.qualifier.LoggedIn;
import at.jit.remind.web.ui.controller.base.EntityListControllerBase;
import at.jit.remind.web.ui.controller.base.EntityListDataModelBase;
import at.jit.remind.web.ui.controller.security.LoginController;
import at.jit.remind.web.ui.util.Navigator;

@LoggedIn
@Named
@SessionScoped
public class ActionLogListController extends EntityListControllerBase<ActionLog>
{
	private static final long serialVersionUID = 6033922033359921378L;

	private static final String createdOnUpperFilterName = "createdOn.upper";
	private static final String createdOnLowerFilterName = "createdOn.lower";
	private static final String logLevelFilterName = "logLevel";
	private static final String logTextFilterName = "logText";

	@Inject
	private Logger logger;

	@Inject
	private Navigator navigator;

	@Inject
	private LoginController loginController;

	@EJB
	private ActionLogService actionLogService;

	@EJB
	private ActionService actionService;

	private ActionLogListDataModel dataModel;

	private InCollectionFilter<ActionLog, Long> actionsFilter;

	@PostConstruct
	protected void initialize()
	{
		dataModel = new ActionLogListDataModel();
		dataModel.setEntityService(actionLogService);

		actionsFilter = new InCollectionFilter<ActionLog, Long>("action.id");
		dataModel.addParameter("actionId", actionsFilter);

		// pre-defined ordering on createdOn is descending
		dataModel.addParameter(createdOnLowerFilterName, new GreaterThanOrEqualFilter<ActionLog, Date>("createdOn", Date.class));
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();

		dataModel.addParameter(createdOnUpperFilterName, new LessThanOrEqualFilter<ActionLog, Date>("createdOn", Date.class));
		dataModel.addParameter(logLevelFilterName, new ToStringLikeFilter<ActionLog>(logLevelFilterName));
		dataModel.addParameter(logTextFilterName, new ToStringLikeFilter<ActionLog>(logTextFilterName));
	}

	public void resetFilters()
	{
		// pre-defined ordering on createdOn is descending
		dataModel.getParameter(createdOnLowerFilterName).clear();
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();

		dataModel.getParameter(createdOnUpperFilterName).clear();
		dataModel.getParameter(logLevelFilterName).clear();
		dataModel.getParameter(logTextFilterName).clear();
	}

	public String navigateTo(long actionId)
	{
		manageActionIdFilter(actionId);

		return navigator.navigateTo("/actionLogList");
	}

	public void manageActionIdFilter(long actionId)
	{
		if (actionId == 0)
		{
			actionsFilter.clear();
		}
		else
		{
			resetFiltersAndSetAscendingOrdering();

			actionsFilter.setValue(actionService.getCurrentAndChildActionIds(actionId, new ArrayList<Long>()));
		}

		setPage(1);
	}

	private void resetFiltersAndSetAscendingOrdering()
	{
		// When user navigates to other pages with implicit filtering, date order shall be ascending
		resetFilters();
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();
	}

	@Override
	public ActionLogListDataModel getDataModel()
	{
		return dataModel;
	}

	public ActionLogListDataModel getModel()
	{
		return getDataModel();
	}

	public void download()
	{
		logger.info("actionLogListController.download() called");

		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) faces.getExternalContext().getResponse();
		response.setContentType("application/force-download");
		response.setHeader("Content-disposition", "inline; filename=\"Logs_Report-" + new Date() + ".csv\"");

		try
		{
			ServletOutputStream os = response.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);

			osw.append("RemInD Logs Report:\n").append("Generated at ").append(new SimpleDateFormat().format(new Date())).append(" by user ")
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

	public InCollectionFilter<ActionLog, Long> getActionsFilter()
	{
		return actionsFilter;
	}

	public void setActionsFilter(InCollectionFilter<ActionLog, Long> actionsFilter)
	{
		this.actionsFilter = actionsFilter;
	}

	private static final class ActionLogListDataModel extends EntityListDataModelBase<ActionLog>
	{
		private ActionLogListDataModel()
		{
			super(new CriteriaQueryParameterHolder<ActionLog>(ActionLog.class));
		}

		public void exportHeader(Writer writer) throws IOException
		{
			writer.append("Filter values: ").append("\n\n");
			writer.append("Date").append(';').append("Level").append(';').append("Text").append("\n\n");
		}

		public void exportData(Writer writer) throws IOException
		{
			int pageSize = 10;
			for (int i = 0; i < getRowCount(); i += pageSize)
			{
				List<ActionLog> results = loadData(i, pageSize);
				if (results.size() == 0)
				{
					break;
				}

				for (ActionLog actionLog : results)
				{
					writer.append(actionLog.getCreatedOn().toString()).append(';').append(actionLog.getLogLevel()).append(';').append(actionLog.getLogText())
							.append('\n');
				}
			}
		}
	}
}
