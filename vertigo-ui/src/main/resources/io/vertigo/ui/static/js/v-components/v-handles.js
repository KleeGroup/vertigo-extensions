Vue.component('v-handles', {
	template : `
	<div>
		<q-input :placeholder="$q.lang.vui.handles.placeholder" v-model="text"  :debounce="300" @input="searchHandles" autofocus outlined bg-color="white" dense >
			<template v-slot:prepend><q-icon name="search" /></template>
		</q-input>
		<q-list bordered dense separator>
			<q-item clickable v-ripple v-for="handle in handles" @click="VUi.methods.goTo(baseUrl + 'hw/' + handle.code )" >
				<q-item-section>
				{{handle.code}}
				</q-item-section>
			</q-item>
		</q-list>
	</div>
	`
	,
	data: function() {
		return {
			text: "",
			handles: []
		}
	},
	props : {
		baseUrl : { type: String, 'default': '/' },
	},
	methods : {
		searchHandles : function(val) {
			if(val) {
				this.$http.post(this.baseUrl+'api/vertigo/handle/_search', {prefix: val} )
			        .then( function (response) { //Ok
			        	this.$data.handles = response.body;
				});
			}
		}
	} 
})
