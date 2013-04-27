package controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import models.Discussion;
import models.Message;
import models.User;
import models.User2Discussion;

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;

import play.libs.Json;
import play.mvc.Result;

public class User2DiscussionREST extends ItemREST {

	public static Result getDiscussions() {

		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		User2Discussion user2disc = MorphiaObject.datastore.get(User2Discussion.class, user.id);
		if (user2disc == null) {
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		}
		List<Discussion> discussions = user2disc.all();
		if (discussions.size() == 0) {
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		} else {
			// Return the response
			return ok(Json.toJson(Discussion.discussionsToObjectNodes(discussions)));
		}
	}
	
	
	public static Result getUnreadDiscussions(){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		User2Discussion user2disc = MorphiaObject.datastore.get(User2Discussion.class, user.id);
		if (user2disc == null) {
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		}
		List<Discussion> discussions = user2disc.unread();
		if (discussions.size() == 0) {
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		} else {
			// Return the response
			return ok(Json.toJson(Discussion.discussionsToObjectNodes(discussions)));
		}
	}

	
	public static Result getDiscussion(String discussionId) {
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		User2Discussion user2disc = MorphiaObject.datastore.get(User2Discussion.class, user.id);
		if (user2disc == null) {
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		}
		Discussion discussion = user2disc.findDiscussionById(discussionId);
		if (discussion == null) {
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		}
		// Return the response
		return ok(Json.toJson(Discussion.discussionToFullObjectNode(discussion)));
	}
	
	public static Result getMessage(String discussionId, String messageId) {
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		User2Discussion user2disc = MorphiaObject.datastore.get(User2Discussion.class, user.id);
		Discussion discussion = user2disc.findDiscussionById(discussionId);
		if (discussion == null) {
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		}
		Message message = discussion.findMessageById(messageId);
		if (message == null){
			return badRequest(Constants.MESSAGES_EMPTY.toString());
		}
		
		// Return a copy of the message
		return ok(Json.toJson(Message.messageToObjectNode(message)));
	}
	
	public static Result addDiscussion() {
		final User user = Application.getLocalUser(session());

		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		
		Discussion discussion = null;
		Message message = null;
		// List to save them if everything went ok
		List<User2Discussion> user2discList = new ArrayList<User2Discussion>(); 
		
		try {
			
		discussion = new Discussion();		// Create discussion
		discussion.messageIds = new ArrayList<ObjectId>();
		discussion.userIds = new ArrayList<ObjectId>();
		discussion.subject = json.findPath("subject").getTextValue();
		discussion.save();
		
		message = new Message();		// Create message
		message.message = json.findPath("message").getTextValue();
		message.writerId = user.id;
		message.save();
		
		discussion.addMessage(message); // Add message to discussion
		
		// Add discussion to all readers
		Iterator<JsonNode> userIds = json.findPath("users").getElements();

		while(userIds.hasNext()){
			String userId = userIds.next().toString().replace("\"", "");
			
			User receiver = MorphiaObject.datastore.get(User.class, new ObjectId(userId));
			
			System.out.println("paso + " + receiver);
			
			if (receiver != null){
				User2Discussion user2disc = MorphiaObject.datastore.get(User2Discussion.class, receiver.id);
				
				// If is users first discussion, create new User2Discussion
				if (user2disc == null){
					user2disc = new User2Discussion();
					user2disc.id = receiver.id;
					user2disc.discussionIds = new ArrayList<ObjectId>();
					user2disc.unread = new ArrayList<ObjectId>();
					user2disc.save();
				}
				
				discussion.addUser(receiver);
				user2disc.addDiscussion(discussion); // Add discussions id to this user
				user2disc.setRead(discussion, false);
				System.out.println("paso + " + user2disc);
				user2discList.add(user2disc);
				
				
			}
		}
		
		// Add discussion to sender
		User2Discussion user2disc = MorphiaObject.datastore.get(User2Discussion.class, user.id);
		
		// If is users first discussion, create new User2Discussion
		if (user2disc == null){
			user2disc = new User2Discussion();
			user2disc.id = user.id;
			user2disc.discussionIds = new ArrayList<ObjectId>();
			user2disc.unread = new ArrayList<ObjectId>();
			user2disc.save();
		}
		
		discussion.addUser(user);
		user2disc.addDiscussion(discussion);
		user2disc.setRead(discussion, true);
		user2discList.add(user2disc);

		} catch (Exception e) {
			
			// TODO REMOVE DISCUSSION, MESSAGE AND USER2DISCUSSIONS
			
			return badRequest(Constants.JSON_EMPTY.toString());
		}
			message.save(); // Save Message
			discussion.save(); // Save discussion
			
			for(final User2Discussion user2disc : user2discList)
				user2disc.save(); // Save user2discussions
				
		//Return a copy of the discussion
		return ok(Json.toJson(Discussion.discussionToFullObjectNode(discussion)));
	}
	
	public static Result reply(String id) {
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		Discussion discussion = Discussion.findById(id);
		if (discussion == null || !discussion.userIds.contains(user.id)){
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		}
		
		Message message = new Message();
		message.message = json.findPath("message").getTextValue();
		message.writerId = user.id;
		message.save();
		
		discussion.addMessage(message);
		discussion.save(); // Save discussion
		
		// Create a list of all the users except me
		List <ObjectId> otherUsers = discussion.userIds;
		otherUsers.remove(user.id);
		
		setUsersDiscussionUnread(discussion, otherUsers);
		
		// Return a copy of the discussion
		return ok(Json.toJson(Discussion.discussionToFullObjectNode(discussion)));
	}
	
	public static Result replyToMessage(String id, String msgId){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		Discussion discussion = Discussion.findById(id);
		if (discussion == null || !discussion.userIds.contains(user.id)){
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		}
		
		// If the message replying to its really from this discussion
		Message msg = discussion.findMessageById(msgId);
		if(msg != null){
			Message message = new Message();
			message.message = json.findPath("message").getTextValue();
			message.replyToMsg = msg.id;
			message.writerId = user.id;
			message.save(); // Save Message
			
			discussion.addMessage(message); // Add message to its discussion
			discussion.save(); // Save Discussion
			
			// Create a list of all the users except me
			List <ObjectId> otherUsers = discussion.userIds;
			otherUsers.remove(user.id);
			
			setUsersDiscussionUnread(discussion, otherUsers);
			
			// Return a copy of the discussion
			return ok(Discussion.discussionToFullObjectNode(discussion));
		}
		return badRequest(Constants.MESSAGES_EMPTY.toString());
	}
	
	/** Change discussion to unread for this users
	 * @param discussionId
	 * @param userIds
	 */
	public static void setUsersDiscussionUnread(Discussion discussion, List<ObjectId> userIds){
		
		for (ObjectId oid : userIds){
			User2Discussion user2disc = MorphiaObject.datastore.get(User2Discussion.class, oid);
			
			// If is users first discussion, create new User2Discussion
			if (user2disc == null){
				user2disc = new User2Discussion();
				user2disc.id = oid;
				user2disc.discussionIds = new ArrayList<ObjectId>();
				user2disc.unread = new ArrayList<ObjectId>();
			}
			user2disc.setRead(discussion, false);
		}
	}
}
