<html>
	
	<head>
		<title>Quoddy: Create Calendar Feed Subscription</title>
		<meta name="layout" content="main" />
	     <nav:resources />		
	</head>
	
	<body>
	<div class="hero-unit span6">	
		<h2>Create Calendar Feed Subscription</h2>
		<g:form controller="subscription" action="createWizard" method="POST">
			<label for="calFeedName">Name:</label> <g:textField name="calFeedName" value=""/>
			<br />
			<label for="calFeedUrl">URL:</label> <g:textField name="calFeedUrl" value=""/>
			<br />
			<g:submitButton name="stage2" class="btn btn-large" value="Save" />
		</g:form>
		</div>
	</body>
</html>