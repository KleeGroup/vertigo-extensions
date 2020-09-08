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
package io.vertigo.quarto.plugins.exporter.xls;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFFooter;
import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HeaderFooter;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.BasicType;
import io.vertigo.core.locale.MessageText;
import io.vertigo.datamodel.smarttype.SmartTypeManager;
import io.vertigo.datamodel.smarttype.definitions.SmartTypeDefinition;
import io.vertigo.datamodel.structure.definitions.DtField;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datastore.entitystore.EntityStoreManager;
import io.vertigo.quarto.exporter.model.Export;
import io.vertigo.quarto.exporter.model.ExportField;
import io.vertigo.quarto.exporter.model.ExportSheet;
import io.vertigo.quarto.impl.exporter.util.ExporterUtil;

/**
 * Export XLS.
 * Uses POI.
 *
 * @author pchretien, npiedeloup
 */
final class XLSExporter {
	private static final int MAX_COLUMN_WIDTH = 50;

	private final Map<DtField, Map<Object, String>> referenceCache = new HashMap<>();
	private final Map<DtField, Map<Object, String>> denormCache = new HashMap<>();

	private final Map<BasicType, HSSFCellStyle> evenHssfStyleCache = new EnumMap<>(BasicType.class);
	private final Map<BasicType, HSSFCellStyle> oddHssfStyleCache = new EnumMap<>(BasicType.class);

	private final EntityStoreManager entityStoreManager;
	private final SmartTypeManager smartTypeManager;

	/**
	 * Constructor.
	 * @param storeManager Store manager
	 */
	XLSExporter(final EntityStoreManager entityStoreManager, final SmartTypeManager smartTypeManager) {
		Assertion.check()
				.isNotNull(entityStoreManager)
				.isNotNull(smartTypeManager);
		//-----
		this.entityStoreManager = entityStoreManager;
		this.smartTypeManager = smartTypeManager;
	}

	private static HSSFCellStyle createHeaderCellStyle(final HSSFWorkbook workbook) {
		final HSSFCellStyle cellStyle = workbook.createCellStyle();
		final HSSFFont font = workbook.createFont();
		font.setFontHeightInPoints((short) 10);
		font.setFontName("Arial");
		font.setBold(true);
		cellStyle.setFont(font);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		cellStyle.setVerticalAlignment(VerticalAlignment.JUSTIFY);
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		cellStyle.setFillForegroundColor(HSSFColorPredefined.GREY_40_PERCENT.getIndex());
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		return cellStyle;
	}

	private static HSSFCellStyle createRowCellStyle(final HSSFWorkbook workbook, final boolean odd) {
		final HSSFCellStyle cellStyle = workbook.createCellStyle();
		final HSSFFont font = workbook.createFont();
		font.setFontHeightInPoints((short) 10);
		font.setFontName("Arial");
		cellStyle.setFont(font);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		cellStyle.setFillForegroundColor(odd ? HSSFColorPredefined.WHITE.getIndex() : HSSFColorPredefined.GREY_25_PERCENT.getIndex());

		return cellStyle;
	}

	/**
	 * Réalise l'export des données de contenu et de la ligne d'en-tête.
	 *
	 * @param parameters Paramètre de cet export
	 * @param workbook Document excel
	 * @param sheet Feuille Excel
	 * @param forceLandscape Indique si le parametrage force un affichage en paysage
	 */
	private void exportData(final ExportSheet parameters, final HSSFWorkbook workbook, final HSSFSheet sheet, final boolean forceLandscape) {
		// Column width
		final Map<Integer, Double> maxWidthPerColumn = new HashMap<>();
		if (parameters.hasDtObject()) {
			exportObject(parameters, workbook, sheet, maxWidthPerColumn);
		} else {
			exportList(parameters, workbook, sheet, maxWidthPerColumn);
		}
		// On definit la largeur des colonnes:
		double totalWidth = 0;
		int cellIndex;
		for (final Map.Entry<Integer, Double> entry : maxWidthPerColumn.entrySet()) {
			cellIndex = entry.getKey();
			final Double maxLength = entry.getValue();
			final int usesMaxLength = Double.valueOf(Math.min(maxLength.doubleValue(), MAX_COLUMN_WIDTH)).intValue();
			sheet.setColumnWidth(cellIndex, usesMaxLength * 256);
			totalWidth += usesMaxLength;
		}
		/**
		 * @todo ne serait-il pas plus simple d'utilisersheet.autoSizeColumn(i); de poi 3.0.1 ?
		 */

		// note: il ne semble pas simple de mettre title et author dans les propriétés du document
		final String title = parameters.getTitle();
		if (title != null) {
			final HSSFHeader header = sheet.getHeader();
			header.setLeft(title);
		}
		sheet.setHorizontallyCenter(true);
		sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
		if (forceLandscape || totalWidth > 85) {
			sheet.getPrintSetup().setLandscape(true);
		}

		// On définit le footer
		final HSSFFooter footer = sheet.getFooter();
		footer.setRight("Page " + HeaderFooter.page() + " / " + HeaderFooter.numPages());
	}

	private void initHssfStyle(final HSSFWorkbook workbook) {
		// default:
		final HSSFCellStyle oddCellStyle = createRowCellStyle(workbook, true);
		final HSSFCellStyle evenCellStyle = createRowCellStyle(workbook, true);
		oddHssfStyleCache.put(BasicType.Boolean, oddCellStyle);
		oddHssfStyleCache.put(BasicType.String, oddCellStyle);
		evenHssfStyleCache.put(BasicType.Boolean, evenCellStyle);
		evenHssfStyleCache.put(BasicType.String, evenCellStyle);

		// Nombre sans décimal
		final HSSFCellStyle oddLongCellStyle = createRowCellStyle(workbook, true);
		final HSSFCellStyle evenLongCellStyle = createRowCellStyle(workbook, true);
		oddLongCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
		evenLongCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
		oddHssfStyleCache.put(BasicType.Long, oddLongCellStyle);
		oddHssfStyleCache.put(BasicType.Integer, oddLongCellStyle);
		evenHssfStyleCache.put(BasicType.Long, evenLongCellStyle);
		evenHssfStyleCache.put(BasicType.Integer, evenLongCellStyle);

		// Nombre a décimal
		final HSSFCellStyle oddDoubleCellStyle = createRowCellStyle(workbook, true);
		final HSSFCellStyle evenDoubleCellStyle = createRowCellStyle(workbook, true);
		oddDoubleCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.00"));
		evenDoubleCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.00"));
		oddHssfStyleCache.put(BasicType.Double, oddDoubleCellStyle);
		oddHssfStyleCache.put(BasicType.BigDecimal, oddDoubleCellStyle);
		evenHssfStyleCache.put(BasicType.Double, evenDoubleCellStyle);
		evenHssfStyleCache.put(BasicType.BigDecimal, evenDoubleCellStyle);

		// Date
		final HSSFCellStyle oddDateCellStyle = createRowCellStyle(workbook, true);
		final HSSFCellStyle evenDateCellStyle = createRowCellStyle(workbook, true);
		oddDateCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy" /* "m/d/yy h:mm" */));
		evenDateCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy" /* "m/d/yy h:mm" */));
		oddHssfStyleCache.put(BasicType.LocalDate, oddDateCellStyle);
		evenHssfStyleCache.put(BasicType.LocalDate, evenDateCellStyle);

		// Instant
		final HSSFCellStyle oddDateTimeCellStyle = createRowCellStyle(workbook, true);
		final HSSFCellStyle evenDateTimeCellStyle = createRowCellStyle(workbook, true);
		oddDateTimeCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
		evenDateTimeCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
		oddHssfStyleCache.put(BasicType.Instant, oddDateTimeCellStyle);
		evenHssfStyleCache.put(BasicType.Instant, evenDateTimeCellStyle);

	}

	private void exportList(final ExportSheet parameters, final HSSFWorkbook workbook, final HSSFSheet sheet, final Map<Integer, Double> maxWidthPerColumn) {
		// exporte le header
		final HSSFRow headerRow = sheet.createRow(0);
		int cellIndex = 0;
		for (final ExportField exportColumn : parameters.getExportFields()) {
			final HSSFCell cell = headerRow.createCell(cellIndex);
			final String displayedLabel = exportColumn.getLabel().getDisplay();
			cell.setCellValue(new HSSFRichTextString(displayedLabel));
			cell.setCellStyle(createHeaderCellStyle(workbook));

			updateMaxWidthPerColumn(displayedLabel, 1.2, cellIndex, maxWidthPerColumn); // +20% pour les majuscules
			cellIndex++;
		}
		//La premiere ligne est répétable
		sheet.setRepeatingRows(new CellRangeAddress(0, 0, -1, -1));

		int rowIndex = 1;
		for (final DtObject dto : parameters.getDtList()) {
			final HSSFRow row = sheet.createRow(rowIndex);
			cellIndex = 0;
			Object value;
			for (final ExportField exportColumn : parameters.getExportFields()) {
				final HSSFCell cell = row.createCell(cellIndex);

				value = ExporterUtil.getValue(entityStoreManager, smartTypeManager, referenceCache, denormCache, dto, exportColumn);
				putValueInCell(smartTypeManager, value, cell, rowIndex % 2 == 0 ? evenHssfStyleCache : oddHssfStyleCache, cellIndex, maxWidthPerColumn, exportColumn.getDtField().getSmartTypeDefinition());

				cellIndex++;
			}
			rowIndex++;
		}
	}

	private void exportObject(final ExportSheet parameters, final HSSFWorkbook workbook, final HSSFSheet sheet, final Map<Integer, Double> maxWidthPerColumn) {
		int rowIndex = 0;
		final int labelCellIndex = 0;
		final int valueCellIndex = 1;
		final DtObject dto = parameters.getDtObject();
		Object value;
		for (final ExportField exportColumn : parameters.getExportFields()) {
			final HSSFRow row = sheet.createRow(rowIndex);

			final HSSFCell cell = row.createCell(labelCellIndex);
			final MessageText label = exportColumn.getLabel();
			cell.setCellValue(new HSSFRichTextString(label.getDisplay()));
			cell.setCellStyle(createHeaderCellStyle(workbook));
			updateMaxWidthPerColumn(label.getDisplay(), 1.2, labelCellIndex, maxWidthPerColumn); // +20% pour les majuscules

			final HSSFCell valueCell = row.createCell(valueCellIndex);
			value = ExporterUtil.getValue(entityStoreManager, smartTypeManager, referenceCache, denormCache, dto, exportColumn);
			putValueInCell(smartTypeManager, value, valueCell, oddHssfStyleCache, valueCellIndex, maxWidthPerColumn, exportColumn.getDtField().getSmartTypeDefinition());
			rowIndex++;
		}

	}

	private static void putValueInCell(
			final SmartTypeManager smartTypeManager,
			final Object value,
			final HSSFCell cell,
			final Map<BasicType, HSSFCellStyle> rowCellStyle,
			final int cellIndex,
			final Map<Integer, Double> maxWidthPerColumn,
			final SmartTypeDefinition smartTypeDefinition) {
		String stringValueForColumnWidth;
		cell.setCellStyle(rowCellStyle.get(smartTypeDefinition.getBasicType()));
		if (value != null) {
			stringValueForColumnWidth = String.valueOf(value);
			if (value instanceof String) {
				final String stringValue = (String) value;
				cell.setCellValue(new HSSFRichTextString(stringValue));
			} else if (value instanceof Integer) {
				final Integer integerValue = (Integer) value;
				cell.setCellValue(integerValue.doubleValue());
			} else if (value instanceof Double) {
				final Double dValue = (Double) value;
				cell.setCellValue(dValue.doubleValue());
				stringValueForColumnWidth = String.valueOf(Math.round(dValue.doubleValue() * 100) / 100D);
			} else if (value instanceof Long) {
				final Long lValue = (Long) value;
				cell.setCellValue(lValue.doubleValue());
			} else if (value instanceof BigDecimal) {
				final BigDecimal bigDecimalValue = (BigDecimal) value;
				cell.setCellValue(bigDecimalValue.doubleValue());
				stringValueForColumnWidth = String.valueOf(Math.round(bigDecimalValue.doubleValue() * 100) / 100D);
			} else if (value instanceof Boolean) {
				final Boolean bValue = (Boolean) value;
				//cell.setCellValue(bValue.booleanValue() ? "Oui" : "Non");
				cell.setCellValue(smartTypeManager.valueToString(smartTypeDefinition, bValue));
			} else if (value instanceof LocalDate) {
				final LocalDate dateValue = (LocalDate) value;
				// sans ce style "date" les dates apparaîtraient au format
				// "nombre"
				cell.setCellValue(dateValue);
				stringValueForColumnWidth = "DD/MM/YYYY";
				// ceci ne sert que pour déterminer la taille de la cellule, on a pas besoin de la vrai valeur
			} else if (value instanceof Instant) {
				final Instant instantValue = (Instant) value;
				cell.setCellValue(LocalDateTime.ofInstant(instantValue, ZoneId.of("UTC")));
				stringValueForColumnWidth = "DD/MM/YYYY HH:mm";
				// ceci ne sert que pour déterminer la taille de la cellule, on a pas besoin de la vrai valeur
			} else {
				throw new UnsupportedOperationException("Type " + smartTypeDefinition.getJavaClass() + " not supported by this Excel exporter");
			}
			updateMaxWidthPerColumn(stringValueForColumnWidth, 1, cellIndex, maxWidthPerColumn); // +20% pour les majuscules
		}
	}

	private static void updateMaxWidthPerColumn(final String value, final double textSizeCoeff, final int cellIndex, final Map<Integer, Double> maxWidthPerColumn) {
		// Calcul de la largeur des colonnes
		final double newLenght = value != null ? value.length() * textSizeCoeff + 2 : 0; // +textSizeCoeff% pour les majuscules, et +2 pour les marges
		final Double oldLenght = maxWidthPerColumn.get(cellIndex);
		if (oldLenght == null || oldLenght.doubleValue() < newLenght) {
			maxWidthPerColumn.put(cellIndex, newLenght);
		}
	}

	/**
	 * Méthode principale qui gère l'export d'un tableau vers un fichier ODS.
	 *
	 * @param documentParameters Paramètres du document à exporter
	 * @param out Flux de sortie
	 * @throws IOException Io exception
	 */
	void exportData(final Export documentParameters, final OutputStream out) throws IOException {
		// Workbook
		final boolean forceLandscape = Export.Orientation.Landscape == documentParameters.getOrientation();
		try (final HSSFWorkbook workbook = new HSSFWorkbook()) {
			initHssfStyle(workbook);
			for (final ExportSheet exportSheet : documentParameters.getSheets()) {
				final String title = exportSheet.getTitle();
				final HSSFSheet sheet = title == null ? workbook.createSheet() : workbook.createSheet(title);
				exportData(exportSheet, workbook, sheet, forceLandscape);

			}
			workbook.write(out);
		}
	}
}
