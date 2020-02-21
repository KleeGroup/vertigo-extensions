Vue.component('v-json-editor', {
	template : 
'	<div class="row">'
+'		<div v-for="(value, key) in jsonAsObject" :key="key" :class="\'col-\'+(12/cols)">'
+'			<q-input  v-if="!readonly" :label="key" orientation="vertical" stack-label  v-model="jsonAsObject[key]" @input="updateJson" ></q-input>'
+ '			<q-field v-else :label="key" orientation="vertical" stack-label borderless readonly>'
+'				<span >{{value}}</span>'					
+'			</q-field>'
+'		</div>'
+'	</div>'
	,
	props : {
		value: { type: String, required: true},
		readonly : { type: Boolean, required: true },
		cols : { type: Number, 'default': 2 }
	},
	data : function () {
		return {
			jsonAsObject : JSON.parse(this.$props.value)
		}
	},
	watch: { 
      	value: function(newVal, oldVal) {
      		this.$data.jsonAsObject = JSON.parse(newVal);
      	}
	},
	methods: {
	    updateJson() {
	      this.$emit('input', JSON.stringify(this.$data.jsonAsObject));
	    }
	  }
})
