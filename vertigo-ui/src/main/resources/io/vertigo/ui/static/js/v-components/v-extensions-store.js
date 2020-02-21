Vue.component('v-extensions-store', {
	template : 
'<div class="row q-col-gutter-md">'
+'	<div v-for="extension in extensions" class="col-xs-12 col-lg-6 col-xl-4"><q-card>'
+'		<q-item  class="bg-white" style="height: 100px;" >'
+'			<q-item-section avatar>'
+'				<q-icon :name="extension.icon" size="40px" :style="getIconStyle(extension.color)"></q-icon>'
+'			</q-item-section>'
+'			<q-item-section>'
+'				<div class="row col items-center"><div class="q-subheading text-bold">{{extension.label}}</div><div class="col"></div><div><q-toggle disable readonly color="positive" v-model="extension.enabled"></q-toggle></div></div>'
+'				<div class="row col q-body-2 text-justify">{{extension.description}}</div>'
+'			</q-item-section>'
+'		</q-item>'
+'	</q-card></div>'
+'</div>'
	,
	props : {
		activeSkills : { type: Array, required : true }
	},
	data: function() {
	    return {
	        extensions: [],
	    }
	},
	created: function() {
		var availableExtensions = [
				{name : "vertigo-audit", label : "Audit", description:"Trace every single aspect of your app through exhaustive logging capabilities.", color:"#F7578C", icon : "fas fa-clipboard-list", enabled : false },
				{name : "vertigo-dashboard", label : "Dashboard", description:"Monitor you system to make sure your app meets the customer requirements.", color:"#742774", icon : "fas fa-chart-line", enabled : false },
				{name : "vertigo-geo", label : "Geo", description:"Enhance your data by enabling geographic functions and tools." , icon : "fas fa-globe", color:"#0E2947", enabled : false },
				{name : "vertigo-ledger", label : "Ledger", description:"Use a blockchain to enforce secure transactions in your app !" , icon : "fas fa-link", color:"#00AC5C", enabled : false },
				{name : "vertigo-orchestra", label : "Orchestra", description:"Manage jobs and monitor their status with this powerfull control tower.", color:"#FC636B", icon : "fas fa-tasks", enabled : false },
				{name : "vertigo-quarto", label : "Quarto", description:"Generate slick documents and reports using the Quarto template engine.", color:"#0747A6", icon : "fas fa-file-invoice", enabled : false },
				{name : "vertigo-social", label : "Social", description:"Ensure real time communication and collaboration between your app users.", color:"#FF3366", icon : "far fa-comments", enabled : false },
				{name : "vertigo-stella", label : "Stella", description:"Enable multi-node task dispatching for your app and assign specific tasks to each node.", color:"#0066FF", icon : "fas fa-network-wired", enabled : false }
		];
		
    	availableExtensions.forEach(function (availableExtension) {
    		availableExtension.enabled = this.$props.activeSkills.indexOf(availableExtension.name) > -1 ;
    	}.bind(this));

    	this.extensions = availableExtensions;
	},
	methods :  {
		getIconStyle : function (color) {
			return 'border: 3px solid '+ color+'; background-color: '+color+'; color: white; padding: 5px; width: 70px; height: 70px;';
		}
	}
})
