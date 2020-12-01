<template>
    <div :id="id" >
        <slot v-on="$listeners"></slot>
    </div>
</template>
<script>
import Quasar from "quasar"
import * as ol from "ol"


export default {
    props : {
        id: { type: String, required: true},
        initialZoomLevel : { type: Number},
        initialCenter : { type: Object },
    },
    methods: {
          onMapLoad: function(found) {
            var vm = this;
            function checkForMap() {
              if (vm.olMap) {
                found(vm.olMap);
                vm.postInit()
              } else {
                setTimeout(checkForMap, 50);
              }
            }
            checkForMap();
          },
          postInit() {
              if (this.$props.initialZoomLevel) {
                    this.olMap.getView().setZoom(this.$props.initialZoomLevel);
               }
          }
    },
    mounted : function() {        
        var view = new ol.View();
        
        var osmLayer = new ol.layer.Tile({
            preload : 4,
            source : new ol.source.OSM()
        })
        this.olMap = new ol.Map({
            interactions: ol.interaction.defaults({
              onFocusOnly: true
            }),
            target : this.$props.id,
            layers : [ osmLayer ],
            // Improve user experience by loading tiles while animating. Will make animations stutter on mobile or slow devices.
            loadTilesWhileAnimating : true,
            view : view
        });
        
        if (this.$props.initialCenter) {
            this.olMap.getView().setCenter(ol.proj.fromLonLat([this.$props.initialCenter.lon, this.$props.initialCenter.lat]));
        }
        
        // handle refresh if an endPoint is specified
        this.olMap.on('moveend', function(e) {
            var mapExtent =  e.map.getView().calculateExtent();
            var wgs84Extent = ol.proj.transformExtent(mapExtent, 'EPSG:3857', 'EPSG:4326');
            var topLeft = ol.extent.getTopLeft(wgs84Extent);
            var bottomRight = ol.extent.getBottomRight(wgs84Extent);
            Quasar.utils.debounce(this.$emit('moveend',topLeft, bottomRight) , 300);        
        }.bind(this));
        
        setTimeout(function () {
            this.olMap.on('click', function(evt) {
                evt.stopPropagation();
                Quasar.utils.debounce(this.$emit('click',ol.proj.transform(evt.coordinate, 'EPSG:3857', 'EPSG:4326')) , 300);
            }.bind(this)); 
        }.bind(this), 300); 
    }
}
</script>