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
package io.vertigo.geo.impl.geocoder;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.geo.geocoder.GeoCoderManager;
import io.vertigo.geo.geocoder.GeoLocation;

/**
 * @author spoitrenaud
 *
 */
public final class GeoCoderManagerImpl implements GeoCoderManager {
	private final GeoCoderPlugin geoCoderPlugin;

	/**
	 * Constructeur.
	 * @param geoCoderPlugin Plugin de Geocoding
	 */
	@Inject
	public GeoCoderManagerImpl(final GeoCoderPlugin geoCoderPlugin) {
		Assertion.check().isNotNull(geoCoderPlugin);
		//-----
		this.geoCoderPlugin = geoCoderPlugin;

	}

	/** {@inheritDoc} */
	@Override
	public GeoLocation findLocation(final String address) {
		return geoCoderPlugin.findLocation(address);
	}

	/** {@inheritDoc} */
	@Override
	public double distanceKm(final GeoLocation geoLocation1, final GeoLocation geoLocation2) {
		Assertion.check()
				.isFalse(geoLocation1.isUndefined(), "le premier point n'est pas défini")
				.isFalse(geoLocation2.isUndefined(), "le second point n'est pas défini");
		//-----
		final int R = 6371; // km
		final double theta = Math.toRadians(geoLocation2.getLongitude() - geoLocation1.getLongitude());
		final double lat1 = Math.toRadians(geoLocation1.getLatitude());
		final double lat2 = Math.toRadians(geoLocation2.getLatitude());
		return Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(theta)) * R;

	}

}
