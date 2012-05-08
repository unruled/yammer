package org.fogbeam.quoddy

/* a UserStream object defines one specific named "stream" of entries that will
 * appear in the user's feed.  The UserStream controls which users, which groups, etc.
 * are included, as well as an filters or other preferences that will affect which items
 * are included in the stream.
 */
class UserStream implements Serializable
{
	public static final String DEFINED_USER = "DEFINED_BY_USER";
	public static final String DEFINED_SYSTEM = "DEFINED_BY_SYSTEM";
	public static final String DEFAULT_STREAM = "Default";
	
	
	public UserStream()
	{
		this.uuid = java.util.UUID.randomUUID().toString();
	}
	
	static constraints = {
		
		description(nullable:true);
			
	}
	
	static hasMany = [ userUuidsIncluded:String, 
					   userListUuidsIncluded:String, 
					   userGroupUuidsIncluded:String,
					   subscriptionUuidsIncluded:String];
	
	String 	name;
	String 	uuid;
	String 	definedBy;
	User 	owner;
	Date 	dateCreated;
	String 	description;
	
	Boolean includeAllEventTypes = false;
	Set<String> userUuidsIncluded;
	Set<String> userListUuidsIncluded;
	Set<String> userGroupUuidsIncluded;
	Set<String> subscriptionUuidsIncluded;
	
	// include:
		// event types
		// users
		// user lists
		// groups
		// subscriptions

	
	// exclude (filters)
		// event types
		// users
		// user lists
		// groups
		// subscriptions
		// content
	
}
