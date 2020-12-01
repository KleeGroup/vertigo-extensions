var VUiExtensions = VUiExtensions || {};
Vue.use(VertigoUi, {axios : axios});

VertigoUi.initData(VertigoUi, JSON.parse(window.document.getElementById('vui-init-data').innerHTML));
Quasar.lang.set(Quasar.lang[VertigoUi.vuiLang]);
var VUiPage = new Vue({
  el: '#page',
  data: {
		  vueData : VertigoUi.vueData,
		  componentStates : VertigoUi.componentStates,
		  uiMessageStack: VertigoUi.uiMessageStack,
		  dataX : VUiExtensions.dataX,
		  vuiLang : VertigoUi.vuiLang
  },
  methods: Vue.util.extend(VertigoUi.methods, VUiExtensions.methods ||  {}),
  created : function() {
	  this.$http.interceptors.response.use(function(response) {
	        return response;
	     }, function(error) {
	          this.onAjaxError(error.response);
	    }.bind(this))
  }
  
});

// fixes wrong components states due to firefox bfcache (back-forward cache)
//https://developer.mozilla.org/en-US/docs/Archive/Misc_top_level/Working_with_BFCache
window.addEventListener('pageshow', function(event) {
	VUiPage.$forceUpdate();
});
