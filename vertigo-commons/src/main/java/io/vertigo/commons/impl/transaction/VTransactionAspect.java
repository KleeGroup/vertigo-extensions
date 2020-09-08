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
package io.vertigo.commons.impl.transaction;

import javax.inject.Inject;

import io.vertigo.commons.transaction.Transactional;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.aop.Aspect;
import io.vertigo.core.node.component.aop.AspectMethodInvocation;

/**
 * Aspect pour la gestion des transactions au niveau
 * de la couche service.
 * @author prahmoune
 */
public final class VTransactionAspect implements Aspect {
	private final VTransactionManager transactionManager;

	/**
	 * Constructor
	 * @param transactionManager the transactionManager
	 */
	@Inject
	public VTransactionAspect(final VTransactionManager transactionManager) {
		Assertion.check().isNotNull(transactionManager);
		//-----
		this.transactionManager = transactionManager;
	}

	@Override
	public Object invoke(final Object[] args, final AspectMethodInvocation methodInvocation) {
		//La transaction est REQUIRED : si elle existe on l'utilise, sinon on la crée.
		if (transactionManager.hasCurrentTransaction()) {
			return methodInvocation.proceed(args);
		}
		//Dans le cas ou il n'existe pas de transaction on en crée une.
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Object o = methodInvocation.proceed(args);
			transaction.commit();
			return o;
		}
	}

	@Override
	public Class<Transactional> getAnnotationType() {
		return Transactional.class;
	}
}
