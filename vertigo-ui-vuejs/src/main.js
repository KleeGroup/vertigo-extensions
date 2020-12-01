import Quasar from 'quasar'
import VChatbot from './components/VChatbot.vue'
import VCommands from './components/VCommands.vue'
import VComments from './components/VComments.vue'
import VExtensionsStore from './components/VExtensionsStore.vue'
import VFacets from './components/VFacets.vue'
import VGeopointInput from './components/VGeopointInput.vue'
import VHandles from './components/VHandles.vue'
import VJsonEditor from './components/VJsonEditor.vue'
import VNotifications from './components/VNotifications.vue'
import VMap from './components/VMap.vue'
import VMapLayer from './components/VMapLayer.vue'

import VMinify from './directives/VMinify'
import VScrollSpy from './directives/VScrollSpy'

import VMethods from './methods'

import EnUs from './lang/vertigo-ui-lang-en-us'
import Fr from './lang/vertigo-ui-lang-fr'

export function getBoundMethods(obj, methods) {
    let boundMethods = {};
    Object.keys(methods)
        .forEach(methodName => boundMethods[methodName] = methods[methodName].bind(obj));
    return boundMethods;
}

export function install (Vue , options) {
      
    // components
    Vue.component("v-chatbot", VChatbot);
    Vue.component("v-commands", VCommands);
    Vue.component("v-comments", VComments);
    Vue.component("v-extensions-store", VExtensionsStore);
    Vue.component("v-facets", VFacets);
    Vue.component("v-geopoint-input", VGeopointInput);
    Vue.component("v-handles", VHandles);
    Vue.component("v-json-editor", VJsonEditor);
    Vue.component("v-notifications", VNotifications);
    Vue.component("v-map", VMap);
    Vue.component("v-map-layer", VMapLayer);
    
    // directives
    Vue.directive("minify", VMinify);
    Vue.directive("scroll-spy", VScrollSpy);

    if (!options.axios) {
        console.error('You have to install axios')
        return
    }

    Vue.axios = options.axios;
    Vue.$http = options.axios;

    Object.defineProperties(Vue.prototype, {
        axios: {
            get() {
            return options.axios
            }
        },
    
        $http: {
            get() {
            return options.axios
            }
        },

        $vui: {
            get() {
                return getBoundMethods(this, VMethods);
            }
        }
    });
    
    
    if (Quasar.lang.enUs) {
        Quasar.lang.enUs.vui = EnUs;
    }
    if (Quasar.lang.fr) {
        Quasar.lang.fr.vui = Fr;
    }
    
   
}

export let methods = VMethods

export function initData(instance, json) {
    instance.vueData = json.vueData;
	instance.componentStates = json.componentStates;
    instance.uiMessageStack = json.uiMessageStack;
    instance.vuiLang = json.vuiLang;
}