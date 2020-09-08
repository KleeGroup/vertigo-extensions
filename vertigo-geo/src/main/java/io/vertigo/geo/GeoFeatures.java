/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.geo;

import io.vertigo.core.node.config.Feature;
import io.vertigo.core.node.config.Features;
import io.vertigo.core.param.Param;
import io.vertigo.geo.geocoder.GeoCoderManager;
import io.vertigo.geo.geosearch.GeoSearchManager;
import io.vertigo.geo.impl.geocoder.GeoCoderManagerImpl;
import io.vertigo.geo.impl.geosearch.GeoSearchManagerImpl;
import io.vertigo.geo.plugins.geocoder.ban.BanGeoCoderPlugin;
import io.vertigo.geo.plugins.geocoder.google.GoogleGeoCoderPlugin;
import io.vertigo.geo.plugins.geosearch.es.ESGeoSearchPlugin;

/**
 * Defines the 'geo' extension
 * @author mlaroche
 */
public final class GeoFeatures extends Features<GeoFeatures> {

	/**
	 * Construtor;
	 */
	public GeoFeatures() {
		super("vertigo-geo");
	}

	/**
	 * Activates notifications
	 * @return the features
	 */
	@Feature("geocoding")
	public GeoFeatures withGeoCoder() {
		getModuleConfigBuilder().addComponent(GeoCoderManager.class, GeoCoderManagerImpl.class);
		return this;
	}

	/**
	 * Activates geosearch
	 * @return the features
	 */
	@Feature("geosearch")
	public GeoFeatures withGeoSearch() {
		getModuleConfigBuilder().addComponent(GeoSearchManager.class, GeoSearchManagerImpl.class);
		return this;
	}

	/**
	 * Activates comments
	 * @return the features
	 */
	@Feature("geocoding.google")
	public GeoFeatures withGoogleGeoCoder() {
		getModuleConfigBuilder().addPlugin(GoogleGeoCoderPlugin.class);
		return this;
	}

	/**
	 * Activates comments
	 * @return the features
	 */
	@Feature("geocoding.ban")
	public GeoFeatures withBanGeoCoder(final Param... params) {
		getModuleConfigBuilder().addPlugin(BanGeoCoderPlugin.class, params);
		return this;
	}

	/**
	 * Activates comments
	 * @return the features
	 */
	@Feature("geosearch.es")
	public GeoFeatures withESGeosearch(final Param... params) {
		getModuleConfigBuilder().addPlugin(ESGeoSearchPlugin.class, params);
		return this;
	}

	@Override
	protected void buildFeatures() {
		// nothing
	}
}
