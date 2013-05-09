package org.fogbeam.quoddy

import org.fogbeam.quoddy.stream.ActivityStreamItem;
import org.fogbeam.quoddy.stream.StreamItemComment;
import org.fogbeam.quoddy.stream.StreamItemBase;


class CommentController {

	def scaffold = false;
	
	def eventStreamService;
	// def entryService;
	
	def addComment = {
			
		println "addComment params: ${params}";
		
		// lookup the Event by id
		log.debug( "eventId: ${params.eventId}" );
		StreamItemBase event = eventStreamService.getEventById( Integer.parseInt( params.eventId) );
			
		// add the comment to the Event
		if( session.user )
		{
			log.debug( "event: ${event}" );
			def user = User.findByUserId( session.user.userId );
			log.debug( "user: ${user}" );
		
			StreamItemComment newComment = new StreamItemComment();
			newComment.text = params.addCommentTextInput;
			newComment.creator = user;
			newComment.event = event;
			newComment.save();
			
			event.addToComments( newComment );
			
			eventStreamService.saveActivity( (ActivityStreamItem)event );
			
	    	// send JMS message saying "new entry submitted"
	    	/* def newCommentMessage = [msgType:"NEW_COMMENT", entry_id:entry.id, entry_uuid:entry.uuid, 
	    	                       	comment_id:newComment.id, comment_uuid:newComment.uuid, comment_text:newComment.text ];
	          */
			
	    	// send a JMS message to our testQueue
			// sendJMSMessage("searchQueue", newCommentMessage );			
			
			log.debug( "saved StreamItemComment for user ${user.userId}, event ${event.id}" );
		}
		else
		{
			// do nothing, can't comment if you're not logged in.
			log.info( "doing nothing, not logged in!" );
		}
	
		// render using template, so we can ajaxify the loading of the comments...
		render( template:"/renderComments", model:[comments:event.comments]);
		
	}
}