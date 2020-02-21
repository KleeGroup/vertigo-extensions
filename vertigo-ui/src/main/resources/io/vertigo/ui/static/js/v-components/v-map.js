Vue.component('v-map', {
	template : '<div :id="id" >'
+'					<div id="popup">'
+'						<q-card  v-if="popupDisplayed" class="q-px-md">'
+'							<div class="text-subtitle2">{{popupTitle}}</div>'
+'						</q-card>'
+'					</div>'			
+'				</div>'
,
	props : {
		id: { type: String, required: true},
		list : { type: Array, required: true },
		field: { type: String, required: true},
		nameField: { type: String, required: true},
		markerColor : { type: String, 'default': "#000000" },
		markerFont : { type: String, 'default': "Material Icons" },
		markerIcon : { type: String, 'default': "place" },
		markerSize : { type: String, 'default': "45px" }
	},
	data : function () {
		return {
			popupDisplayed : false,
			popupTitle: ''
		}
	},
	mounted : function() {
		var view = new ol.View();

		var styleIcon = new ol.style.Style({
			text : new ol.style.Text({
				font : this.$props.markerSize +' ' + this.$props.markerFont,
				text : this.$props.markerIcon,
				fill : new ol.style.Fill({color : this.$props.markerColor })
			})
		});

		var geoField = this.$props.field;
		var nameField = this.$props.nameField;
		features = this.$props.list.map(function(object) {
			var geoObject;
			if (typeof object[geoField] === 'string' || object[geoField] instanceof String){
				geoObject = JSON.parse(object[geoField]);
			} else {
				geoObject = object[geoField];
			}
			var iconFeature = new ol.Feature({
				geometry : new ol.geom.Point(ol.proj.fromLonLat([ geoObject.lon, geoObject.lat ])),
			});
			
			iconFeature.set('name', object[nameField]);
			
			iconFeature.setStyle(styleIcon);
			return iconFeature
		});

		var vectorSource = new ol.source.Vector({
			features : features
		});

		var vectorLayer = new ol.layer.Vector({
			source : vectorSource
		});
		
		var osmLayer = new ol.layer.Tile({
			preload : 4,
			source : new ol.source.OSM()
		})
		var map = new ol.Map({
			interactions: ol.interaction.defaults({
	          onFocusOnly: true
	        }),
			target : this.$props.id,
			layers : [ osmLayer, vectorLayer ],
			// Improve user experience by loading tiles while animating. Will make animations stutter on mobile or slow devices.
			loadTilesWhileAnimating : true,
			view : view
		});
		
		// fit view
		if (features.length > 0) {
			map.getView().fit(vectorLayer.getSource().getExtent(), map.getSize());
		}
		
		var popup = new ol.Overlay({
	        element: this.$el.querySelector('#popup'),
	        positioning: 'bottom-center',
	        stopEvent: false,
	        offset: [0, -10]
	      });
	    map.addOverlay(popup);
		// display popup on click
        map.on('click', function(evt) {
          var feature = map.forEachFeatureAtPixel(evt.pixel,
            function(feature) {
              return feature;
            });
          if (feature) {
            var coordinates = feature.getGeometry().getCoordinates();
            popup.setPosition(coordinates);
            this.$data.popupDisplayed = true;
            this.$data.popupTitle = feature.get('name');
          } else {
          	this.$data.popupDisplayed = false;
          }
        }.bind(this));
	      
	    // change mouse cursor when over marker
	    map.on('pointermove', function(e) {
			if (e.dragging) {
				this.$data.popupDisplayed = false;
			  return;
			}
			var pixel = map.getEventPixel(e.originalEvent);
			var hit = map.hasFeatureAtPixel(pixel);
			map.getTargetElement().style.cursor = hit ? 'pointer' : '';
		}.bind(this));
		

	}
})
