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
package io.vertigo.vega.plugins.webservice.scanner.annotations;

import java.util.List;

import io.vertigo.vega.impl.webservice.WebServiceScannerPlugin;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.definitions.WebServiceDefinition;

/**
 * @author npiedeloup
 */
public final class AnnotationsWebServiceScannerPlugin implements WebServiceScannerPlugin {

	/** {@inheritDoc} */
	@Override
	public List<WebServiceDefinition> scanWebService(final Class<? extends WebServices> webServicesClass) {
		return AnnotationsWebServiceScannerUtil.scanWebService(webServicesClass);
	}
}
