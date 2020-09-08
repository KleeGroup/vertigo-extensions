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
package io.vertigo.audit.impl.trace;

import java.util.List;

import javax.inject.Inject;

import io.vertigo.audit.trace.Trace;
import io.vertigo.audit.trace.TraceCriteria;
import io.vertigo.audit.trace.TraceManager;
import io.vertigo.core.lang.Assertion;

/**
 * @author xdurand
 */
public final class TraceManagerImpl implements TraceManager {
	private final TraceStorePlugin auditTraceStorePlugin;

	/**
	 * Constructor.
	 * @param auditTraceStorePlugin
	 */
	@Inject
	public TraceManagerImpl(final TraceStorePlugin auditTraceStorePlugin) {
		Assertion.check().isNotNull(auditTraceStorePlugin);
		//---
		this.auditTraceStorePlugin = auditTraceStorePlugin;
	}

	@Override
	public void addTrace(final Trace auditTrace) {
		auditTraceStorePlugin.create(auditTrace);
	}

	@Override
	public List<Trace> findTrace(final TraceCriteria auditTraceCriteria) {
		return auditTraceStorePlugin.findByCriteria(auditTraceCriteria);
	}

	@Override
	public Trace getTrace(final Long auditTraceId) {
		return auditTraceStorePlugin.read(auditTraceId);
	}

}
