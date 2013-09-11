/** Disables command links and file upload component on deployment.xhtml */
function disableComponents() {
	jQuery('a#userInputForm\\:resetLink').replaceWith(function(){
        return $("<span>" + $(this).html() + "</span>");
    });
	jQuery('a#userInputForm\\:applyLink').replaceWith(function(){
        return $("<span>" + $(this).html() + "</span>");
    });
	jQuery('a#contentPanelForm\\:validateLink').replaceWith(function(){
        return $("<span>" + $(this).html() + "</span>");
    });
	jQuery('a#contentPanelForm\\:deployLink').replaceWith(function(){
        return $("<span>" + $(this).html() + "</span>");
    });
	
	// disables file upload component
	jQuery('.rf-fu-hdr').replaceWith(function(){
        return $("<div class='rf-fu-hdr'><span class='rf-fu-btns-lft'><span class='rf-fu-btn-add-dis'><span class='rf-fu-btn-cnt-add-dis'>Select file...</span></span></span></div>");
    });
	
	// activate indicator for running deployment
	jQuery('img#contentPanelForm\\:activeDeploymentIndicator').replaceWith(function(){
        return $("<img id='contentPanelForm:activeDeploymentIndicator' style='float:left; margin-top: -5px;' src='resources/images/deploymentActive_true.gif'>");
    });
	
	jQuery('a#consoleActionsForm\\:exportConsoleLink').replaceWith(function(){
        return $("<span>" + $(this).html() + "</span>");
    });
}