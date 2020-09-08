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
package io.vertigo.quarto.impl.publisher.merger.script;

/**
 * Tag Script.
 * @author pchretien, npiedeloup
 */
public interface ScriptTag {
	/**
	 * @param content tag évalué
	 * @param context Context d'évaluation
	 * @return Contenu a substituer au tag
	 */
	String renderOpen(ScriptTagContent content, ScriptContext context);

	/**
	 * @param content tag évalué
	 * @param context Context d'évaluation
	 * @return Contenu a substituer au tag
	 */
	String renderClose(ScriptTagContent content, ScriptContext context);
}
