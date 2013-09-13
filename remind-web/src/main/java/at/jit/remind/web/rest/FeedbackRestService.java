package at.jit.remind.web.rest;

import java.io.File;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.solder.logging.Logger;

import at.jit.remind.core.context.messaging.FeedbackContext;
import at.jit.remind.web.domain.messaging.service.FeedbackHandler;

@Path("/feedback")
public class FeedbackRestService
{
	@Inject
	private Logger logger;

	@Inject
	private FeedbackHandler feedbackHandler;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/context")
	public FeedbackContext[] getAllContexts()
	{
		logger.info("FeedbackRestService: GET /context");

		return feedbackHandler.getAllFeedbackContexts().toArray(new FeedbackContext[]{});
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/context/{identifier}")
	public FeedbackContext getContext(@PathParam("identifier") String identifier)
	{
		logger.info("FeedbackRestService: GET /context: " + identifier);

		return feedbackHandler.getFeedbackContext(identifier);
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/context/source/{identifier}")
	public Response getSource(@PathParam("identifier") String identifier)
	{
		logger.info("FeedbackRestService: GET /context/source: " + identifier);

		ResponseBuilder response;

		FeedbackContext feedbackContext = feedbackHandler.getFeedbackContext(identifier);
		if (feedbackContext != null)
		{
			File file = new File(feedbackContext.getData(FeedbackContext.soureFilePathDataKey));

			response = Response.ok((Object) file);
			response.header("Content-Disposition", "attachment; filename=source.txt");
		}
		else
		{
			response = Response.noContent();
		}

		return response.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/context/errorcause/{identifier}")
	public Response getErrorCause(@PathParam("identifier") String identifier)
	{
		logger.info("FeedbackRestService: GET /context/errorcause: " + identifier);

		ResponseBuilder response;

		FeedbackContext feedbackContext = feedbackHandler.getFeedbackContext(identifier);
		if (feedbackContext != null)
		{
			String errorCause = feedbackContext.getData(FeedbackContext.errorCauseDataKey);

			response = Response.ok(errorCause.getBytes(), MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition",
					"attachment; filename=errorcause.txt");
		}
		else
		{
			response = Response.noContent();
		}

		return response.build();
	}

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Path("/skip")
	public void skip(String identifier)
	{
		logger.info("FeedbackRestService: POST /skip: " + identifier);
		feedbackHandler.skip(identifier);
		logger.info("Skipped: " + identifier);
	}

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Path("/skipStatement")
	public void skipStatement(String identifier)
	{
		logger.info("FeedbackRestService: POST /skipStatement " + identifier);
		feedbackHandler.skipStatement(identifier);
		logger.info("Statement skipped: " + identifier);
	}

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Path("/abort")
	public void abort(String identifier)
	{
		logger.info("FeedbackRestService: POST /abort " + identifier);
		feedbackHandler.abort(identifier);
		logger.info("Aborted: " + identifier);
	}
}
