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
package io.vertigo.vega.webservice.data.domain;

public enum Honorific {
	Mr("MR_"),
	Miss("MIS"),
	Mrs("MRS"),
	Ms("MS_"),
	Dr("DR_"),
	Cpt("CAP"),
	Cch("CCH"),

	Off("OFF"),
	Rev("REV"),
	Fth("FTH"),
	PhD("PHD"),
	Mst("MST");

	private final String code;

	private Honorific(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
