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
package io.vertigo.ui.impl.thymeleaf;

import java.util.HashSet;
import java.util.Set;

import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;

import io.vertigo.core.lang.Assertion;
import io.vertigo.ui.impl.thymeleaf.components.ContentItemComponentProcessor;
import io.vertigo.ui.impl.thymeleaf.components.ContentSlotComponentProcessor;
import io.vertigo.ui.impl.thymeleaf.components.NamedComponentDefinition;
import io.vertigo.ui.impl.thymeleaf.components.NamedComponentElementProcessor;
import io.vertigo.ui.impl.thymeleaf.components.OnceAttributeTagProcessor;
import io.vertigo.ui.impl.thymeleaf.components.SlotAttributeTagProcessor;
import io.vertigo.ui.impl.thymeleaf.components.SlotComponentProcessor;

public final class VUiStandardDialect extends AbstractProcessorDialect {

	public static final String NAME = "VertigoStandard";
	public static final String PREFIX = "vu";
	public static final int PROCESSOR_PRECEDENCE = 2000;

	// These variables will be initialized lazily following the model applied in the extended StandardDialect.
	private final Set<NamedComponentDefinition> components;

	public VUiStandardDialect(final Set<NamedComponentDefinition> components) {
		super(NAME, PREFIX, PROCESSOR_PRECEDENCE);
		Assertion.check().isNotNull(components);
		//---
		this.components = components;
	}

	@Override
	public Set<IProcessor> getProcessors(final String dialectPrefix) {
		return createVUiStandardProcessorsSet(dialectPrefix);
	}

	private Set<IProcessor> createVUiStandardProcessorsSet(final String dialectPrefix) {
		final Set<IProcessor> processors = new HashSet<>();
		processors.add(new OnceAttributeTagProcessor(dialectPrefix));
		processors.add(new ContentItemComponentProcessor(dialectPrefix));
		processors.add(new SlotAttributeTagProcessor(dialectPrefix));
		processors.add(new SlotComponentProcessor(dialectPrefix));
		processors.add(new ContentSlotComponentProcessor(dialectPrefix));

		//standard components
		for (final NamedComponentDefinition comp : components) {
			processors.add(new NamedComponentElementProcessor(dialectPrefix, comp));
		}
		return processors;

	}

}
