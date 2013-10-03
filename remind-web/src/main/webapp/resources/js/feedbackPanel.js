function render(url, identifier) {
	var feedbackUrl = url.toLowerCase() + '/context/' + identifier;
	jQuery.get(feedbackUrl, function (data) {
		jQuery("pre#feedback").text(data.dataMap["error.message"]);
		
   		// check if array doesn't contains "Abort"
   		if (jQuery.inArray("Abort", data.availableFeedbacks) == -1) 
   		{   	
   			jQuery("a#feedbackFormSkip\\:abort").hide();
   		} 
   		else 
   		{
   			// if array contains "Abort" feedback, set link label to value of "Abort" key
   			jQuery("a#feedbackFormSkip\\:abort").text(data.feedbackLabelMap["Abort"]);
   		}
   		
   		// check if array doesn't contains "Skip"
   		if (jQuery.inArray("Skip", data.availableFeedbacks) == -1) 
   		{   	
   			jQuery("a#feedbackFormSkip\\:skip").hide();
   		}
   		else 
   		{
   		// if array contains "Skip" feedback, set link label to value of "Skip" key
   			jQuery("a#feedbackFormSkip\\:skip").text(data.feedbackLabelMap["Skip"]);
   		}

   		// check if array doesn't contains "SkipStatement"
   		if (jQuery.inArray("SkipStatement", data.availableFeedbacks) == -1) 
   		{   	
   			jQuery("a#feedbackFormSkip\\:skipStatement").hide();
   			jQuery('a#downloadFile').hide();
   			jQuery('a#downloadStatement').hide();
   		}
   		else 
   		{
   		// if array contains "SkipStatement" feedback, set link label to value of "SkipStatement" key
   			jQuery("a#feedbackFormSkip\\:skipStatement").text(data.feedbackLabelMap["Skip Statement"]);
   			jQuery('a#downloadFile').html("Download File");
   			jQuery('a#downloadStatement').html("Download Statement");
   		}
   		
   		jQuery("input#fixInput").val(data.dataMap["error.cause"]);
   		
   	});
}