Vue.component('v-map2', {
	template : '<div :id="id" >'
+'					<div id="popup">'
+'						<q-card  v-if="popupDisplayed" class="q-px-md">'
+'							<slot name="card" v-bind:objectDisplayed="objectDisplayed">'
+'    							<div class="text-subtitle2">{{objectDisplayed[nameField]}}</div>'
+' 							</slot>'	
+'						</q-card>'
+'					</div>'			
+'				</div>'
,
	props : {
		id: { type: String, required: true},
		list : { type: Array },
		baseUrl : { type: String },
		field: { type: String, required: true},
		nameField: { type: String},
		initialZoomLevel : { type: Number},
		initialCenter : { type: Object },
		markerColor : { type: String, 'default': "#000000" },
		markerFont : { type: String, 'default': "Material Icons" },
		markerIcon : { type: String, 'default': "place" },
		markerSize : { type: Number, 'default': 45 },
		clusterCircleSize: { type: Number, 'default': 20 },
		clusterColor: { type: String, 'default': "#fff" },
		clusterCircleBorderColor: { type: String, 'default': "#000000" },
		clusterTextColor: { type: String, 'default': "#000000" },
		clusterTextSize: { type: Number, 'default': 12 },
		clusterTextFont: { type: String, 'default' : 'sans-serif' },
	},
	data : function () {
		return {
			popupDisplayed : false,
			objectDisplayed: {},
			items: [],
			olMap: {}
		}
	},
	computed : {
		features: function() {
			var geoField = this.$props.field;
			var arrayOfFeatures = this.$data.items.map(function(object) {
				var geoObject;
				if (typeof object[geoField] === 'string' || object[geoField] instanceof String){
					geoObject = JSON.parse(object[geoField]);
				} else {
					geoObject = object[geoField];
				}
				var iconFeature = new ol.Feature({
					geometry : new ol.geom.Point(ol.proj.fromLonLat([ geoObject.lon, geoObject.lat ])),
				});
				
				if (this.$props.nameField) {
					iconFeature.set('name', object[this.$props.nameField]);
					iconFeature.set('innerObject', object);
				}
				return iconFeature
			}.bind(this));
			return arrayOfFeatures;
		}
	},
	methods : {
		fetchList: function(topLeft, bottomRight) {
	        this.$http.get(this.baseUrl+'_geoSearch?topLeft="'+ topLeft.lat+','+topLeft.lon+'"&bottomRight="'+ bottomRight.lat+','+bottomRight.lon+ '"', { timeout:5*1000, })
	        .then( function (response) { //Ok
	        	this.$data.items = response.body;
	        	this.olMap.getLayers().getArray()[1].getSource().getSource().clear();
	        	this.olMap.getLayers().getArray()[1].getSource().getSource().addFeatures(this.features);
			});
	    }
		
	},
	mounted : function() {
		this.$data.items = this.$props.list ? this.$props.list : [];
		var view = new ol.View();

		var vectorSource = new ol.source.Vector({
			features : this.features
		});
		
		var clusterSource = new ol.source.Cluster({
	        source: vectorSource,
	        distance : 2*this.$props.clusterCircleSize
	    });
	    var clusterLayer = new ol.layer.Vector({
	        source: clusterSource
	    });
	    
	    var styleIcon = new ol.style.Style({
			text : new ol.style.Text({
				font : this.$props.markerSize +'px ' + this.$props.markerFont,
				text : this.$props.markerIcon,
				fill : new ol.style.Fill({color : this.$props.markerColor }),
				offsetY : -this.$props.markerSize/2
			})
		});
	    
	    var styleCache = {};
	    clusterLayer.setStyle(function(feature, resolution) {
	        var size = feature.get('features').length;
	        if (size == 1) {
	        	return styleIcon;
	        } else {
	              // otherwise show the number of features
	              var style = styleCache[size];
	              if (!style) {
	                style = new ol.style.Style({
	                  image: new ol.style.Circle({
	                    radius: this.$props.clusterCircleSize,
	                    stroke: new ol.style.Stroke({
	                        color: this.$props.clusterCircleBorderColor
	                      }),
	                    fill: new ol.style.Fill({
	                      color: this.$props.clusterColor
	                    })
	                  }),
	                  text: new ol.style.Text({
	                    text: size.toString(),
	                    font : this.$props.clusterTextSize +'px ' + this.$props.clusterTextFont,
	                    fill: new ol.style.Fill({
	                      color: this.$props.clusterTextColor
	                    })
	                  })
	                });
	                styleCache[size] = style;
	              }
	              return style;
	        }
	    }.bind(this));
		
		var osmLayer = new ol.layer.Tile({
			preload : 4,
			source : new ol.source.OSM()
		})
		this.olMap = new ol.Map({
			interactions: ol.interaction.defaults({
	          onFocusOnly: true
	        }),
			target : this.$props.id,
			layers : [ osmLayer, clusterLayer ],
			// Improve user experience by loading tiles while animating. Will make animations stutter on mobile or slow devices.
			loadTilesWhileAnimating : true,
			view : view
		});
		
		// fit view
		if (this.features.length > 0) {
			this.olMap.getView().fit(clusterLayer.getSource().getSource().getExtent(), this.olMap.getSize());
		}
		
		if (this.$props.initialCenter) {
			this.olMap.getView().setCenter(ol.proj.fromLonLat([this.$props.initialCenter.lon, this.$props.initialCenter.lat]));
		}
		if (this.$props.initialZoomLevel) {
			this.olMap.getView().setZoom(this.$props.initialZoomLevel);
		}
		// handle refresh if an endPoint is specified
		if (this.baseUrl) {
			this.olMap.on('moveend', function(e) {
				var mapExtent =  e.map.getView().calculateExtent();
				var wgs84Extent = ol.proj.transformExtent(mapExtent, 'EPSG:3857', 'EPSG:4326');
				var topLeft = ol.extent.getTopLeft(wgs84Extent);
				var bottomRight = ol.extent.getBottomRight(wgs84Extent);
				Quasar.utils.debounce(this.fetchList({lat:topLeft[0] , lon:topLeft[1]},{lat:bottomRight[0] , lon:bottomRight[1]}),300);				
			}
			.bind(this));
		} else {
			this.olMap.on('moveend', function(e) {
				var mapExtent =  e.map.getView().calculateExtent();
				var wgs84Extent = ol.proj.transformExtent(mapExtent, 'EPSG:3857', 'EPSG:4326');
				var topLeft = ol.extent.getTopLeft(wgs84Extent);
				var bottomRight = ol.extent.getBottomRight(wgs84Extent);
				Quasar.utils.debounce(this.$emit('moveend',topLeft, bottomRight) , 300);
			}
			.bind(this));
			this.olMap.on('click', function(evt) {
			    Quasar.utils.debounce(this.$emit('click',ol.proj.transform(evt.coordinate, 'EPSG:3857', 'EPSG:4326')) , 300);
			}.bind(this));
		}
		
		if (this.$props.nameField) {
			var popup = new ol.Overlay({
		        element: this.$el.querySelector('#popup'),
		        positioning: 'bottom-center',
		        stopEvent: false,
		        offset: [0, -10]
		      });
			this.olMap.addOverlay(popup);
			// display popup on click
			this.olMap.on('click', function(evt) {
	          var feature = this.olMap.forEachFeatureAtPixel(evt.pixel,
	            function(feature) {
	              return feature;
	            });
	          if (feature && feature.get('features').length == 1) {
	        	var markerFeature = feature.get('features')[0];
	            var coordinates = feature.getGeometry().getCoordinates();
	            popup.setPosition(coordinates);
	            this.$data.popupDisplayed = true;
	            this.$data.objectDisplayed = feature.get('features')[0].get('innerObject');
	          } else {
	          	this.$data.popupDisplayed = false;
	          }
	        }.bind(this));
		      
		    // change mouse cursor when over marker
			this.olMap.on('pointermove', function(e) {
				if (e.dragging) {
					this.$data.popupDisplayed = false;
				  return;
				}
				var pixel = this.olMap.getEventPixel(e.originalEvent);
				var hit = this.olMap.hasFeatureAtPixel(pixel);
				this.olMap.getTargetElement().style.cursor = hit ? 'pointer' : '';
			}.bind(this));
		}

	}
})
