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
package io.vertigo.social.impl.mail;

import io.vertigo.core.locale.MessageKey;

/**
 * Dictionnaire des ressources.
 *
 * @author  npiedeloup
 */
public enum Resources implements MessageKey {
	/**
	 * L''adresse email {0} n''est pas correct.
	 */
	TEMPO_MAIL_ADRESS_MAIL_INVALID,

	/**
	 * Le serveur mail {0}:{1} ne répond pas.
	 */
	TEMPO_MAIL_SERVER_TIMEOUT,

}
