package org.fogbeam.quoddy;

import static groovyx.net.http.ContentType.TEXT
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient

import org.fogbeam.quoddy.stream.ActivityStreamItem
import org.fogbeam.quoddy.stream.ShareTarget
import org.fogbeam.quoddy.stream.StatusUpdate
import org.fogbeam.quoddy.stream.constants.EventTypes
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH;

class StatusController {

	def userService;
	def eventStreamService;
	def jmsService;
	
	def updateStatus = {
		
		User user = null;
		
		if( !session.user )
		{
			flash.message = "Must be logged in before updating status";
		}
		else
		{
			log.debug( "logged in; so proceeding...");
			
			// get our user
			user = userService.findUserByUserId( session.user.userId );
			
			log.debug( "constructing our new StatusUpdate object...");
			// construct a status object
			log.debug( "statusText: ${params.statusText}");
			StatusUpdate newStatus = new StatusUpdate( text:params.statusText,creator : user);
			newStatus.effectiveDate = new Date(); // now
			newStatus.targetUuid = "ABC123";
			newStatus.name = "321CBA";
			
			
			// Hit Stanbol to get enrichmentData
			// call Stanbol REST API to get enrichment data
			String stanbolServerUrl = CH.config.urls.stanbol.endpoint;
			log.debug( "using stanbolServerUrl: ${stanbolServerUrl}");
			RESTClient restClient = new RESTClient( stanbolServerUrl )
		
			boolean enhancementEnabled = Boolean.parseBoolean( CH.config.features.enhancement.enabled ? CH.config.features.enhancement.enabled : "false" );
			
			if( enhancementEnabled )
			{
				log.trace( "content submitted: ${content}");
				HttpResponseDecorator restResponse = restClient.post(	path:'enhancer',
											body: params.statusText,
											requestContentType : TEXT );
										
				log.debug( "restResponse.class: ${restResponse.class}");
				log.debug( "restResponse.status: ${restResponse.status}");
				log.trace( "restResponse.statusCode: ${restResponse.statusCode}");
				log.debug( "restResponse.success: ${restResponse.isSuccess()}");

				Object restResponseData = restResponse.getData();
			
				if( restResponseData instanceof InputStream )
				{
			
					log.debug( "restResponseData.class: ${restResponseData.class}");
			
					java.util.Scanner s = new java.util.Scanner((InputStream)restResponseData).useDelimiter("\\A");
	
					String restResponseText = s.next();
			
					log.debug( "using Scanner: ${restResponseText}");		
			
					log.trace( "restResponseText: ${restResponseText}");
			
					newStatus.enhancementJSON = restResponseText;
			
				}
				else if( restResponseData instanceof net.sf.json.JSONObject )
				{
					newStatus.enhancementJSON = restResponseData.toString();
				}
				else if( restResponseData instanceof java.lang.String )
				{
					newStatus.enhancementJSON = new String( restResponseData );
				}
				else
				{
					newStatus.enhancementJSON = restResponseData.toString();
				}
			}
			else
			{
				newStatus.enhancementJSON = "";
			}
			
			// save the newStatus 
			if( !newStatus.save() )
			{
				log.debug(( "Saving newStatus FAILED"));
				newStatus.errors.allErrors.each { log.debug( it ) };
			}
			
			// put the old "currentStatus" in the oldStatusUpdates collection
			// addToComments
			if( user.currentStatus != null )
			{
				StatusUpdate previousStatus = user.currentStatus;
				// TODO: do we need to detach this or something?
				user.addToOldStatusUpdates( previousStatus );
			}
			
			// set the current status
			log.debug( "setting currentStatus");
			user.currentStatus = newStatus;
			if( !user.save() )
			{
				log.debug( "Saving user FAILED");
				user.errors.allErrors.each { log.debug( it ) };
			}
			else
			{
				// handle failure to update User
			}
			
			session.user = user;
			
			// TODO: if the user update was successful
			ActivityStreamItem activity = new ActivityStreamItem(content:newStatus.text);
			ShareTarget streamPublic = ShareTarget.findByName( ShareTarget.STREAM_PUBLIC );

			
			activity.title = "Internal ActivityStreamItem";
			activity.url = new URL( "http://www.example.com" );
			activity.verb = "quoddy_status_update";
			activity.actorObjectType = "User";
			activity.actorUuid = user.uuid;
			activity.targetObjectType = "STREAM_PUBLIC";
			activity.published = new Date(); // set published to "now"
			activity.targetUuid = streamPublic.uuid;
			activity.owner = user;
			activity.streamObject = newStatus;
			activity.objectClass = EventTypes.STATUS_UPDATE.name;    
			

			// NOTE: we added "name" to StreamItemBase, but how is it really going
			// to be used?  Do we *really* need this??
			activity.name = activity.title;
			// activity.effectiveDate = activity.published;
			
			eventStreamService.saveActivity( activity );
			
			
			def newContentMsg = [msgType:'NEW_STATUS_UPDATE', activityId:activity.id, activityUuid:activity.uuid ];
				
			log.debug( "sending message to JMS");
			// jmsService.send( queue: 'quoddySearchQueue', msg, 'standard', null );
			sendJMSMessage("quoddySearchQueue", newContentMsg );
			
			jmsService.send( queue: 'uitestActivityQueue', activity, 'standard', null );
			
		}
		
		log.debug( "redirecting to home:index");
		redirect( controller:"home", action:"index", params:[userId:user.userId]);
	}

	def listUpdates =
	{
		User user = null;
		List<StatusUpdate> updates = new ArrayList<StatusUpdate>();
		
		if( !session.user )
		{
			flash.message = "Must be logged in before updating status";
		}
		else
		{
			log.debug( "logged in; so proceeding...");
			
			// get our user
			user = userService.findUserByUserId( session.user.userId );
			
			updates.addAll( user.oldStatusUpdates.sort { it.dateCreated }.reverse() );
		}
		
		[updates:updates]
	}
	
	def deleteStatus =
	{
		String delItemUuid = params.item;
		ActivityStreamItem item = ActivityStreamItem.findByUuid( delItemUuid );
		
		if( item != null )
		{
			log.debug( "found it!" );
			item.delete();
			
		}
		else
		{
			log.debug( "nope" );
		}
		
		render( status: 200 );
	}
	
	
}
