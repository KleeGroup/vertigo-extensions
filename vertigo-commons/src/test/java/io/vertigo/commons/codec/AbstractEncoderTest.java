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
package io.vertigo.commons.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.vertigo.commons.impl.codec.CodecManagerImpl;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;

/**
 * @author dchallas
 * @param <S> Type Source à encoder
 * @param <T> Type cible, résultat de l'encodage
 */
public abstract class AbstractEncoderTest<C extends Encoder<S, T>, S, T> {
	protected static final String TEXT = "Les sanglots longs des violons de l'automne blessent mon coeur d'une langueur monotone.";
	protected C codec;

	@Inject
	private CodecManager codecManager;
	private AutoCloseableNode node;

	@BeforeEach
	public final void setUp() {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
		//---
		codec = obtainCodec(codecManager);
	}

	@AfterEach
	public final void tearDown() throws Exception {
		if (node != null) {
			codec = null;
			//---
			node.close();
		}
	}

	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(ModuleConfig.builder("commons")
						.addComponent(CodecManager.class, CodecManagerImpl.class)
						.build())
				.build();
	}

	protected abstract C obtainCodec(CodecManager inCodecManager);

	/**
	 * test l'encodage et le décodage avec les chaines null.
	 */
	public abstract void testNull();

	/**
	 * test l'encodage de chaines non null.
	 */
	public abstract void testEncode();

	protected final void checkEncode(final S value, final T expectedEncodedValue) {
		final T encodedValue = codec.encode(value);
		assertEquals(expectedEncodedValue, encodedValue);
		checkEncodedValue(encodedValue);
	}

	protected void checkEncodedValue(final T encodedValue) {
		// à implementer si besoin
	}

	protected final CodecManager getCodecManager() {
		return codecManager;
	}

}
