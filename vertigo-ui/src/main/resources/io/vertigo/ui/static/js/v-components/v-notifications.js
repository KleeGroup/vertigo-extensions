Vue.component('v-notifications', {
	template : 
'<q-btn round dense :color="hasNew?\'primary\':\'white\'" :textColor="hasNew?\'white\':\'primary\'" :icon="count>0?icon:iconNone" class="on-left" >'
+'	<q-badge color="red" text-color="white" floating v-if="count>0" >{{count}}</q-badge>'
+'	<q-menu class="notifications">'
+'		<q-list style="width:300px">'
+'          <q-item v-for="notif in list" :key="notif.uuid" tag="a" :href="notif.targetUrl" >'
+'    			<q-item-section avatar><q-icon :name="toIcon(notif.type)" size="2rem"></q-icon></q-item-section>'
+'    			<q-item-section><q-item-label>{{notif.title}}</q-item-label><q-item-label caption lines="3">{{notif.content}}</q-item-label></q-item-section>'
+'				<q-item-section side top>'
+'    				<q-item-label caption>{{toDelay(new Date(notif.creationDate))}}</q-item-label>'
+'    			</q-item-section>'
+'    		</q-item>'
+'  	</q-list>'
+'	</q-menu>'
+'</q-btn>'
	,
	props : {
		icon : { type: String, 'default': 'notifications' },
		iconNone : { type: String, 'default': 'notifications_none' },
		typeIconMap : { type: Object, 'default': function() { return {} } },
		baseUrl : { type: String, 'default': '/api/', required:true }
	},
	data: function() {
	    return {
	    	firstCall : true,
	        list: [],
	        hasNew : false,
	        wasError : false,
	        count: 0,
	        timer: ''
	    }
	},
	created: function() {
	    this.fetchNotificationsList();
	    this.timer = setInterval(this.fetchNotificationsList, 5000)

	},
	methods: {
		fetchNotificationsList: function() {
	        this.$http.get(this.baseUrl+'x/notifications/api/messages', { timeout:5*1000, })
	        .then( function (response) { //Ok
	        	this.updateNotificationsData(response.body);
	        	if(this.wasError) {
	        		clearInterval(this.timer);
	        		this.timer = setInterval(this.fetchNotificationsList, 5000);
	        	}
	        	this.wasError = false;
			})
			.catch(function  (response) { //Ko
	        	if(!this.wasError) {
	        		clearInterval(this.timer);
	        		this.timer = setInterval(this.fetchNotificationsList, 60000);
	        	}
	        	this.wasError = true;
			});
	    },
	    updateNotificationsData : function (newList) {
	    	// Tri par ordre décroissant de date de création
	    	const sortedList = newList.sort(function(a,b) {return  b.creationDate - a.creationDate});
	    	var newElements = [];	    	
	    	// Traverse both arrays simultaneously.
	    	var lastOldElement = this.list[0];
	    	if(!lastOldElement) {
	    		newElements = sortedList;
	    	} else {
	    		for(var newIdx = 0; newIdx < sortedList.length; newIdx++) {
		        	if( sortedList[newIdx].uuid != lastOldElement.uuid) {
			        	if(sortedList[newIdx].creationDate < lastOldElement.creationDate) {
			        		break;
			        	} else {
			        		newElements.push(sortedList[newIdx]);
			        	}
		        	}
		        }
	    	}
	        // Mise à jour des notifications
	    	this.list = sortedList;
	    	// Met à jour le nombre total de notifications
	    	this.count = sortedList.length;
	    	this.hasNew = newElements.length>0;
	    	if(!this.firstCall) {
	    		newElements.forEach(function(notif) {
	    			this.$q.notify({ 
	    				type : 'info',
	    				icon : this.toIcon(notif.type),
	    				message : notif.title,
	    				detail : notif.content,
	    				timeout : 3000,
	    				position : 'bottom-right'
	    			})
	    		}.bind(this));
	    	}
	    	
	    	// Booléen indiquant s'il s'agit du premier appel à la MaJ des notifications
	    	this.firstCall = false;	    	
	    },	    
	    cancelAutoUpdate: function() { clearInterval(this.timer) },
	    toIcon : function(type) {
	    	var typeIcon = this.typeIconMap[type];
	    	return typeIcon?typeIcon:'mail';
	    },
	    toDelay : function(creationDate) {
	    	let diff = Quasar.utils.date.getDateDiff(Date.now(),creationDate, 'days');
	    	if(diff>0)
	    		return diff+' '+ this.$q.lang.vui.notifications.days;
	    	diff = Quasar.utils.date.getDateDiff(Date.now(),creationDate, 'hours');
	    	if(diff>0)
	    		return diff+' '+ this.$q.lang.vui.notifications.hours;
	    	diff = Quasar.utils.date.getDateDiff(Date.now(),creationDate, 'minutes');
	    	if(diff>0)
	    		return diff+' ' +this.$q.lang.vui.notifications.minutes;
	    	diff = Quasar.utils.date.getDateDiff(Date.now(),creationDate, 'seconds');
	    	return diff+' '+this.$q.lang.vui.notifications.seconds;	    	
	    }
	    

	},
	beforeDestroy : function() {
	  clearInterval(this.timer)
	}
})
