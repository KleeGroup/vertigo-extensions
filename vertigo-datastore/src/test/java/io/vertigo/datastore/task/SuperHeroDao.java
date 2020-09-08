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
package io.vertigo.datastore.task;

import java.util.List;
import java.util.Optional;

import io.vertigo.basics.task.TaskEngineProc;
import io.vertigo.basics.task.TaskEngineSelect;
import io.vertigo.commons.transaction.Transactional;
import io.vertigo.core.node.component.Amplifier;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datamodel.task.proxy.TaskContextProperty;
import io.vertigo.datamodel.task.proxy.TaskInput;
import io.vertigo.datamodel.task.proxy.TaskOutput;
import io.vertigo.datamodel.task.proxy.TaskProxyAnnotation;
import io.vertigo.datastore.task.data.domain.SuperHero;

@Transactional
public interface SuperHeroDao extends Amplifier {
	@TaskProxyAnnotation(
			name = "TkCarCount",
			request = "select count(*) from super_hero ",
			taskEngineClass = TaskEngineSelect.class)
	@TaskOutput(smartType = "STyInteger")
	int count();

	@TaskProxyAnnotation(
			name = "TkSuperHeroLoadByName",
			request = "<%if (name !=null) {%>select * from super_hero where name = #name# <%} else {%>"
					+ "select * from super_hero <%}%>",
			taskEngineClass = TaskEngineSelect.class)
	@TaskOutput(smartType = "STyDtSuperHero")
	DtList<SuperHero> findAll(
			@TaskInput(name = "name", smartType = "STyString") Optional<String> nameOpt);

	@TaskProxyAnnotation(
			name = "TkSuperHeroCountByName",
			request = "select count(*) from super_hero where name=#name# ",
			taskEngineClass = TaskEngineSelect.class)
	@TaskOutput(smartType = "STyInteger")
	int count(
			@TaskInput(name = "name", smartType = "STyString") String manufacturer);

	@TaskProxyAnnotation(
			name = "TkLoadSuperHeroNames",
			request = "select distinct name from super_hero ",
			taskEngineClass = TaskEngineSelect.class)
	@TaskOutput(smartType = "STyString")
	List<String> names();

	@TaskProxyAnnotation(
			name = "TkUpdateSuperHeroNames",
			request = "update  super_hero set name =#newName# where name=#oldName#",
			taskEngineClass = TaskEngineProc.class)
	@TaskContextProperty(name = "connectionName", value = "main")
	void update(
			@TaskInput(name = "oldName", smartType = "STyString") String oldName,
			@TaskInput(name = "newName", smartType = "STyString") String newName);

}
