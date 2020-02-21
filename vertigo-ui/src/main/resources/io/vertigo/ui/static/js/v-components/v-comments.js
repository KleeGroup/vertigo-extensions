Vue.component('v-comments', {
	template : 
'<span>'
+'	<q-btn round size="lg" color="primary" textColor="white" :icon="count>0?icon:iconNone" @click="commentDrawer = !commentDrawer" class="on-left" >'
+'		<q-badge floating small color="red" v-if="count>0" style="right:-.4em;top:-.4em;">{{count}}</q-badge>'
+'	</q-btn>'
+'	<q-drawer overlay behavior="mobile" :width="600" v-model="commentDrawer" side="right" style="top:58px;">'
+'		<q-list >'
+'			<q-item-label header><big>{{$q.lang.vui.comments.title}}</big></q-item-label>'
+'			<q-item>'
+'      		<q-item-section><q-input class="col" type="textarea" autogrow v-model="commentTextArea" :label="$q.lang.vui.comments.inputLabel" stack-label></q-input></q-item-section>'
+'				<q-item-section side><q-btn color="primary" round icon="send" :title="$q.lang.vui.comments.actionLabel" :aria-label="$q.lang.vui.comments.actionLabel" @click="publishComment"></q-btn></q-item-section>'
+'      	</q-item>'
+'			<q-separator />'
+'          <q-item v-for="comment in list" :key="comment.uuid" class="items-start" v-bind:class="{\'cursor-pointer\': comment.author==connectedAccount}">'
+'    			<q-item-section avatar> <q-avatar><img :src="baseUrl+\'/x/accounts/api/\'+comment.author+\'/photo\'"></q-avatar></q-item-section>'
+'    			<q-item-section>'
+'					<q-item-label>{{comment.authorDisplayName}}<q-item-label>'
+'					<div>'			
+'						{{comment.msg}}'
+'					</div>'
+'				</q-item-section>'
+'    			<q-item-section side>'
+'    				<q-item-label stamp>{{toDelay(new Date(comment.creationDate))}}</q-item-label>'
+'    				<q-icon name="edit" v-if="comment.author==connectedAccount"></q-icon>'
+'    			</q-item-section>'
+' 						<q-popup-edit v-if="comment.author==connectedAccount" :buttons="true" @save="updateComment(comment)" :label-cancel="$q.lang.vui.comments.cancel" :label-set="$q.lang.vui.comments.save">'
+'    						<q-input type="textarea" autogrow v-model="comment.msg" dense ></q-input>'
+'    					</q-popup-edit>'
+'    		</q-item>'
+'  	</q-list>'
+'	</q-drawer>'
+'</span>'
	,
	props : {
		concept : { type: String },
		id : { type: String },
		icon : { type: String, 'default': 'comment' },
		iconNone : { type: String, 'default': 'add_comment' },
		baseUrl : { type: String, 'default': '/api/', required:true },
		connectedAccount : { type: String }
	},
	data: function() {
	    return {
	    	list: [],
	        count: 0,
	        commentDrawer: false,
	        commentTextArea :""	    
	    }
	},
	created: function() {
	    this.fetchCommentsList();

	},
	methods: {
		fetchCommentsList: function() {
	        this.$http.get(this.baseUrl+'x/comment/api/comments?concept='+this.concept+'&id='+this.id)
	        .then( function (response) { //Ok
	        	this.list = response.body;
	        	this.count = this.list.length;
			});
	    },
	    publishComment: function() {
	    	var newComment = {
	    			msg : this.commentTextArea
	    	};
	    	if (newComment.msg){
		    	newComment.concept = this.concept;
		    	newComment.id = this.id;
		        this.$http.post(this.baseUrl+'x/comment/api/comments?concept='+this.concept+'&id='+this.id, newComment)
		        .then( function (response) { //Ok
		        	this.commentTextArea = '';
		        	this.fetchCommentsList();
				});
	    	}
	    },
	    updateComment: function(newComment) {
	    	this.$http.put(this.baseUrl+'x/comment/api/comments/'+newComment.uuid, newComment)
	        .then( function (response) { //Ok
	        	this.commentTextArea = '';
	        	this.fetchCommentsList();
			});
	    },
	    toDelay : function(creationDate) {
	    	let diff = Quasar.utils.date.getDateDiff(Date.now(),creationDate, 'days');
	    	if(diff>0)
	    		return diff+' days';
	    	diff = Quasar.utils.date.getDateDiff(Date.now(),creationDate, 'hours');
	    	if(diff>0)
	    		return diff+' hours';
	    	diff = Quasar.utils.date.getDateDiff(Date.now(),creationDate, 'minutes');
	    	if(diff>0)
	    		return diff+' min';
	    	return 'Now';	    	
	    }
	},
})