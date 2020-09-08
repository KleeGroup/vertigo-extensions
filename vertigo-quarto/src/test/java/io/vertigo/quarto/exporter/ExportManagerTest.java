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
package io.vertigo.quarto.exporter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.locale.MessageText;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.DefinitionProviderConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.datamodel.DataModelFeatures;
import io.vertigo.datamodel.impl.smarttype.ModelDefinitionProvider;
import io.vertigo.datamodel.structure.model.DtList;
import io.vertigo.datastore.DataStoreFeatures;
import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.quarto.QuartoFeatures;
import io.vertigo.quarto.exporter.data.DtDefinitions.ContinentFields;
import io.vertigo.quarto.exporter.data.DtDefinitions.CountryFields;
import io.vertigo.quarto.exporter.data.TestExporterSmartTypes;
import io.vertigo.quarto.exporter.data.domain.Continent;
import io.vertigo.quarto.exporter.data.domain.Country;
import io.vertigo.quarto.exporter.model.Export;
import io.vertigo.quarto.exporter.model.ExportBuilder;
import io.vertigo.quarto.exporter.model.ExportFormat;

/**
 * Test de l'implémentation standard.
 *
 * @author dchallas
 */
public final class ExportManagerTest {
	// Répertoire de test
	private static String OUTPUT_PATH = "c:/tmp/";

	@Inject
	private ExporterManager exportManager;

	private AutoCloseableNode node;

	@BeforeEach
	public final void setUp() {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
	}

	@AfterEach
	public final void tearDown() {
		if (node != null) {
			node.close();
		}
	}

	private NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLocales("fr_FR")
						.addPlugin(ClassPathResourceResolverPlugin.class)
						.build())
				.addModule(new CommonsFeatures().build())
				.addModule(new DataModelFeatures().build())
				.addModule(new DataStoreFeatures()
						.withCache()
						.withMemoryCache()
						.withEntityStore()
						.build())
				.addModule(new QuartoFeatures()
						.withExporter()
						.withCSVExporter()
						.withPDFExporter()
						.withRTFExporter()
						.withXLSExporter()
						.build())
				.addModule(ModuleConfig.builder("myApp")
						.addDefinitionProvider(DefinitionProviderConfig.builder(ModelDefinitionProvider.class)
								.addDefinitionResource("smarttypes", TestExporterSmartTypes.class.getName())
								.addDefinitionResource("dtobjects", "io.vertigo.quarto.exporter.data.DtDefinitions")
								.build())
						.build())
				.build();
	}

	/**
	 * Test l'export CSV.
	 */
	@Test
	public void testExportHandlerCSV() {
		final DtList<Country> dtc = buildCountries();

		final Export export = new ExportBuilder(ExportFormat.CSV, OUTPUT_PATH + "test.csv")
				.beginSheet(dtc, "famille")
				.endSheet()
				.build();
		final VFile result = exportManager.createExportFile(export);
		nop(result);
	}

	private void nop(final Object o) {
		// nop
	}

	/**
	 * Test l'export CSV d'un objet.
	 */
	@Test
	public void testExportObject() {
		final Country china = new Country().setName("china");

		final Export export = new ExportBuilder(ExportFormat.CSV, OUTPUT_PATH + "test2.csv")
				.beginSheet(china, "china")
				.endSheet()
				.build();
		final VFile result = exportManager.createExportFile(export);
		nop(result);
	}

	/**
	 * Test l'export CSV d'un champs donnée.
	 */
	@Test
	public void testExportField() {
		final Country china = new Country().setName("china");

		final Export export = new ExportBuilder(ExportFormat.CSV, OUTPUT_PATH + "test3.csv")
				.beginSheet(china, "china")
				.addField(CountryFields.name)
				.endSheet()
				.build();

		final VFile result = exportManager.createExportFile(export);
		nop(result);
	}

	/**
	 * Test l'export CSV d'un champs avec label surchargé.
	 */
	@Test
	public void testExportFieldOverrideLabel() {
		final Country china = new Country().setName("china");

		final Export export = new ExportBuilder(ExportFormat.CSV, OUTPUT_PATH + "test3.csv")
				.beginSheet(china, "china")
				.addField(CountryFields.name, MessageText.of("test"))
				.endSheet()
				.build();

		final VFile result = exportManager.createExportFile(export);
		nop(result);
	}

	/**
	 * Test l'export CSV d'un champs avec une dénormalisation de sa valeur.
	 */
	@Test
	public void testExportFieldDenorm() {
		final DtList<Continent> dtc = buildContinents();
		final Country germany = new Country()
				.setId(1L)
				.setConId(10L)
				.setName("germany");

		final Export export = new ExportBuilder(ExportFormat.CSV, OUTPUT_PATH + "test4.csv")
				.beginSheet(germany, "germany")
				.addField(CountryFields.conId, dtc, ContinentFields.name)
				.endSheet()
				.build();

		final VFile result = exportManager.createExportFile(export);
		nop(result);
	}

	/**
	 * Test l'export CSV d'un champs avec une dénormalisation de sa valeur, et
	 * surcharge du label.
	 */
	@Test
	public void testExportFieldDenormOverrideLabel() {
		final DtList<Country> dtc = buildCountries();
		final Country germany = new Country()
				.setId(1L)
				.setName("germany");

		final Export export = new ExportBuilder(ExportFormat.CSV, OUTPUT_PATH + "test5.csv")
				.beginSheet(germany, "country")
				.addField(CountryFields.id, dtc, CountryFields.name, MessageText.of("test"))
				.endSheet()
				.build();

		final VFile result = exportManager.createExportFile(export);
		nop(result);
	}

	/**
	 * Test l'export Excel.
	 */
	@Test
	public void testExportHandlerExcel() {
		final DtList<Country> countries = buildCountries();
		final DtList<Continent> contients = buildContinents();

		final Export export = new ExportBuilder(ExportFormat.XLS, OUTPUT_PATH + "test.xls")
				.beginSheet(countries, "countries")
				.addField(CountryFields.conId, contients, ContinentFields.name)
				.addField(CountryFields.active)
				.addField(CountryFields.localDate)
				.addField(CountryFields.instant)
				.endSheet()
				.build();

		final VFile result = exportManager.createExportFile(export);
		nop(result);
	}

	/**
	 * Test l'export RTF.
	 */
	@Test
	public void testExportHandlerRTF() {
		final DtList<Country> dtc = buildCountries();

		final Export export = new ExportBuilder(ExportFormat.RTF, OUTPUT_PATH + "test.rtf")
				.withAuthor("test")
				.withTitle("test title")
				.beginSheet(dtc, "famille")
				.endSheet()
				.build();

		final VFile result = exportManager.createExportFile(export);
		nop(result);
	}

	/**
	 * Test l'export PDF.
	 */
	@Test
	public void testExportHandlerPDF() {
		final DtList<Country> dtc = buildCountries();

		final Export export = new ExportBuilder(ExportFormat.PDF, OUTPUT_PATH + "test.pdf")
				.beginSheet(dtc, "famille")
				.endSheet()
				.withAuthor("test")
				.build();

		final VFile result = exportManager.createExportFile(export);
		nop(result);
	}

	private static DtList<Continent> buildContinents() {
		final Continent europe = new Continent().setId(10L).setName("Europe");
		final Continent america = new Continent().setId(20L).setName("America");
		final Continent unknownContinent = new Continent().setId(30L); //no name
		final Continent asie = new Continent().setId(40L).setName("Asie");

		final DtList<Continent> dtc = new DtList<>(Continent.class);
		// les index sont données par ordre alpha > null à la fin >
		dtc.add(america);
		dtc.add(asie);
		dtc.add(europe);
		dtc.add(unknownContinent);
		return dtc;
	}

	private static DtList<Country> buildCountries() {
		final Country france = new Country().setId(1L).setConId(10L).setName("France").setActive(true).setLocalDate(LocalDate.of(2018, 10, 15)).setInstant(Instant.now());
		final Country usa = new Country().setId(2L).setConId(20L).setName("usa").setActive(true).setLocalDate(LocalDate.of(2017, 9, 14)).setInstant(Instant.now().minus(2, ChronoUnit.MINUTES));
		final Country unknownCountry = new Country().setId(3L).setConId(30L).setActive(false).setLocalDate(LocalDate.of(2016, 8, 13)).setInstant(Instant.now().minus(4, ChronoUnit.MINUTES)); //no name
		final Country japan = new Country().setId(4L).setConId(40L).setName("japan").setActive(true).setLocalDate(LocalDate.of(2015, 7, 12)).setInstant(Instant.now().minus(6, ChronoUnit.MINUTES));

		final DtList<Country> dtc = new DtList<>(Country.class);
		// les index sont données par ordre alpha > null à la fin >
		dtc.add(france);
		dtc.add(usa);
		dtc.add(unknownCountry);
		dtc.add(japan);
		return dtc;
	}
}
