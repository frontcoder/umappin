var messagesApp = messagesApp || {};
_.templateSettings.variable = "rc";
(function(){
	//this is the view controller for the messages of a discussion

	messagesApp.MessagesView = Backbone.View.extend({

	    template: _.template($('#messages-template').html()),

        events: {
            "click #reply": "reply"
        },

	    render: function() {
	    	//changed to html() because replace, replaced the selected whole tag
	        $(this.el).html(this.template(this.model.toJSON()));
            console.log("rendering");
            return this;
	    },
        reply: function(ev) {
            this.model.attributes.messages.push(this.$el.find('#sending_message').val());
            this.render();
            //this.model.save();
        }
	});
}());