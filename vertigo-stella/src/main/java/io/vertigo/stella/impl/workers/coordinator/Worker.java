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
package io.vertigo.stella.impl.workers.coordinator;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.util.InjectorUtil;
import io.vertigo.stella.impl.work.WorkItem;
import io.vertigo.stella.master.MasterManager;
import io.vertigo.stella.master.WorkResultHandler;

/**
 * Exécution d'un work.
 *
 * @author pchretien, npiedeloup
 * @param<R> result
 * @param <W> Type du work
 */
final class Worker<R, W> implements Callable<R> {

	private static final Logger LOGGER = LogManager.getLogger(MasterManager.class); //même logger que le WorkListenerImpl

	/**
	 * Pour vider les threadLocal entre deux utilisations du Thread dans le pool,
	 * on garde un accès débridé au field threadLocals de Thread.
	 * On fait le choix de ne pas vider inheritedThreadLocal qui est moins utilisé.
	 */
	private static final Field threadLocalsField;

	static {
		try {
			threadLocalsField = Thread.class.getDeclaredField("threadLocals");
		} catch (final SecurityException | NoSuchFieldException e) {
			throw WrappedException.wrap(e);
		}
		threadLocalsField.setAccessible(true);
	}

	private final WorkItem<W, R> workItem;
	private final WorkResultHandler<R> workResultHandler;

	/**
	 * Constructeur.
	 * @param workItem WorkItem à traiter
	 */
	Worker(final WorkItem<W, R> workItem, final WorkResultHandler<R> workResultHandler) {
		Assertion.check()
				.isNotNull(workItem)
				.isNotNull(workResultHandler);
		//-----
		this.workItem = workItem;
		this.workResultHandler = workResultHandler;
	}

	private static <W, R> R executeNow(final WorkItem<W, R> workItem) {
		Assertion.check().isNotNull(workItem);
		//-----
		return InjectorUtil.newInstance(workItem.getWorkEngineClass())
				.process(workItem.getWork());
	}

	/** {@inheritDoc} */
	@Override
	public R call() {
		try {
			workResultHandler.onStart();
			//---
			final R result = executeNow(workItem);
			//---
			workResultHandler.onDone(result, null);
			return result;
		} catch (final Exception e) {
			workResultHandler.onDone(null, e);
			logError(e);
			throw WrappedException.wrap(e, "error calling {0} with id {1}", workItem.getWorkEngineClass(), workItem.getId());
		} finally {
			try {
				//Vide le threadLocal
				cleanThreadLocals();
			} catch (final RuntimeException e) {
				//Ce n'est pas une cause de rejet du Work, on ne fait que logger
				logError(e);
			}
		}
	}

	private void logError(final Throwable e) {
		LOGGER.error("Erreur de la tache de type : " + workItem.getWorkEngineClass().getName(), e);
	}

	/**
	 * Vide le threadLocal du thread avant de le remettre dans le pool.
	 * Ceci protège contre les WorkEngine utilsant un ThreadLocal sans le vider.
	 * Ces workEngine peuvent poser des problémes de fuites mémoires (Ex: le FastDateParser de Talend)
	 * Voir aussi: http://weblogs.java.net/blog/jjviana/archive/2010/06/10/threadlocal-thread-pool-bad-idea-or-dealing-apparent-glassfish-memor
	 */
	private static void cleanThreadLocals() {
		try {
			threadLocalsField.set(Thread.currentThread(), null);
		} catch (final IllegalArgumentException | IllegalAccessException e) {
			throw WrappedException.wrap(e);
		}
	}
}
