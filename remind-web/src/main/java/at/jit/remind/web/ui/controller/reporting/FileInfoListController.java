package at.jit.remind.web.ui.controller.reporting;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.solder.logging.Logger;

import at.jit.remind.web.domain.base.model.CriteriaQueryParameterHolder;
import at.jit.remind.web.domain.base.model.GreaterThanOrEqualFilter;
import at.jit.remind.web.domain.base.model.LessThanOrEqualFilter;
import at.jit.remind.web.domain.base.model.ToStringLikeFilter;
import at.jit.remind.web.domain.context.reporting.model.FileInfo;
import at.jit.remind.web.domain.context.reporting.service.FileInfoService;
import at.jit.remind.web.domain.security.qualifier.LoggedIn;
import at.jit.remind.web.ui.controller.base.EntityListControllerBase;
import at.jit.remind.web.ui.controller.base.EntityListDataModelBase;
import at.jit.remind.web.ui.controller.security.LoginController;

@LoggedIn
@Named
@SessionScoped
public class FileInfoListController extends EntityListControllerBase<FileInfo>
{
	private static final long serialVersionUID = -1153236715195869408L;

	private static final String sessionIdFilterName = "sessionId";
	private static final String md5FilterName = "md5";
	private static final String fileNameFilterName = "name";
	private static final String usernameFilterName = "username";
	private static final String createdOnUpperFilterName = "createdOn.upper";
	private static final String createdOnLowerFilterName = "createdOn.lower";

	@Inject
	private Logger logger;

	@Inject
	private LoginController loginController;

	@EJB
	private FileInfoService fileInfoService;

	private FileInfoListDataModel dataModel;

	@PostConstruct
	protected void initialize()
	{
		dataModel = new FileInfoListDataModel();
		dataModel.setEntityService(fileInfoService);

		// pre-defined ordering on createdOn is descending
		dataModel.addParameter(createdOnLowerFilterName, new GreaterThanOrEqualFilter<FileInfo, Date>("createdOn", Date.class));
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();

		dataModel.addParameter(createdOnUpperFilterName, new LessThanOrEqualFilter<FileInfo, Date>("createdOn", Date.class));
		dataModel.addParameter(usernameFilterName, new ToStringLikeFilter<FileInfo>("user.username"));
		dataModel.addParameter(fileNameFilterName, new ToStringLikeFilter<FileInfo>(fileNameFilterName));
		dataModel.addParameter(md5FilterName, new ToStringLikeFilter<FileInfo>(md5FilterName));
		dataModel.addParameter(sessionIdFilterName, new ToStringLikeFilter<FileInfo>(sessionIdFilterName));
	}

	public void resetFilters()
	{
		// pre-defined ordering on createdOn is descending
		dataModel.getParameter(createdOnLowerFilterName).clear();
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();
		dataModel.getParameter(createdOnLowerFilterName).switchSortOrder();

		dataModel.getParameter(createdOnUpperFilterName).clear();
		dataModel.getParameter(usernameFilterName).clear();
		dataModel.getParameter(fileNameFilterName).clear();
		dataModel.getParameter(md5FilterName).clear();
		dataModel.getParameter(sessionIdFilterName).clear();
	}

	@Override
	public FileInfoListDataModel getDataModel()
	{
		return dataModel;
	}

	public FileInfoListDataModel getModel()
	{
		return getDataModel();
	}

	public void download()
	{
		logger.info("fileInfoListController.download() called");

		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) faces.getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse) faces.getExternalContext().getResponse();
		response.setContentType("application/x-download");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-disposition", "attachment; filename=\"File_Logs_Export-" + new Date() + ".xml\"");

		try
		{
			ServletOutputStream os = response.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);

			boolean isUnix = !request.getHeader("User-Agent").contains("Windows");

			String[] lines = dataModel.getRowData().getContent().split(System.getProperty("line.separator"));

			for (String line : lines)
			{
				if (isUnix)
				{
					line = String.format(line + "%n");
				}
				else
				{
					if (line.contains("\n"))
					{
						line = line.replace("\n", "\r\n");
					}
					else
					{
						line += "\r\n";
					}
				}

				osw.write(line);
			}
			osw.flush();
		}
		catch (IOException e)
		{
			logger.error("fileInfoListController.download(): could not export file.");
		}

		faces.responseComplete();
	}

	private static final class FileInfoListDataModel extends EntityListDataModelBase<FileInfo>
	{
		private FileInfoListDataModel()
		{
			super(new CriteriaQueryParameterHolder<FileInfo>(FileInfo.class));
		}
	}
}
