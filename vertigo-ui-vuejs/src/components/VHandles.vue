<template>
    <div>
        <q-input :placeholder="$q.lang.vui.handles.placeholder" v-model="text"  :debounce="300" @input="searchHandles" autofocus outlined bg-color="white" dense >
            <template v-slot:prepend><q-icon name="search" /></template>
        </q-input>
        <q-list bordered dense separator>
            <q-item clickable v-ripple v-for="handle in handles" @click="VUi.methods.goTo(baseUrl + 'hw/' + handle.code )" :key="handle.code" >
                <q-item-section>
                {{handle.code}}
                </q-item-section>
            </q-item>
        </q-list>
    </div>
</template>
<script>
export default {
    props : {
        baseUrl : { type: String, 'default': '/' },
    },
    data: function() {
        return {
            text: "",
            handles: []
        }
    },
    methods : {
        searchHandles : function(val) {
            if(val) {
                this.$http.post(this.baseUrl+'api/vertigo/handle/_search', {prefix: val} )
                    .then( function (response) { //Ok
                        this.$data.handles = response.data;
                }.bind(this));
            }
        }
    } 
}
</script>