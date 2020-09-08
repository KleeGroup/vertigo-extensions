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
package io.vertigo.quarto.plugins.publisher.odt;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import io.vertigo.datastore.filestore.model.VFile;
import io.vertigo.quarto.impl.publisher.merger.processor.MergerProcessor;
import io.vertigo.quarto.publisher.model.PublisherData;

/**
 * Inversion des textInput dans le fichier ODT.
 * @author npiedeloup
 */
final class ODTImageProcessor implements MergerProcessor {
	private static final String IMAGE_TAG = "(<draw:frame .*draw:name=\")&lt;#image ([a-z][A-Za-z0-9]+)#&gt;(\".*svg:width=\")([0-9\\.]+)(.*\" svg:height=\")([0-9\\.]+)(.*\".*><draw:image xlink:href=\")(Pictures/[0-9A-F\\.a-z]+)(\".*/></draw:frame>)";
	private static final Pattern IMAGE_PATTERN = Pattern.compile(IMAGE_TAG);

	private static final int DUMMY1_GROUP = 1; //(<draw:frame .*draw:name=\")
	private static final int IMAGE_CODE_GROUP = 2; //([a-z][A-Za-z0-9]+)
	private static final int DUMMY2_GROUP = 3; //(\".*svg:width=\")
	private static final int IMAGE_WIDTH_GROUP = 4; //([0-9\\.]+)
	private static final int DUMMY3_GROUP = 5; //(.*\" svg:height=\")
	private static final int IMAGE_HEIGHT_GROUP = 6;//([0-9\\.]+)
	private static final int DUMMY4_GROUP = 7; //(.*\".*><draw:image xlink:href=\")
	private static final int IMAGE_FILENAME_GROUP = 8;//(Pictures/[0-9A-F\\.a-z]+)
	private static final int DUMMY5_GROUP = 9; //(\".*/></draw:frame>)"

	private final Map<String, VFile> newImagesMap = new HashMap<>();

	/** {@inheritDoc} */
	@Override
	public String execute(final String xmlInput, final PublisherData publisherData) throws IOException {
		final StringBuilder xmlOutput = new StringBuilder(xmlInput.length());

		final Matcher imageMatcher = IMAGE_PATTERN.matcher(xmlInput);
		int offset = 0;
		while (imageMatcher.find(offset)) {
			final String imageCode = imageMatcher.group(IMAGE_CODE_GROUP); //([a-z][A-Za-z0-9]+)
			final String imageFileName = imageMatcher.group(IMAGE_FILENAME_GROUP);//(Pictures/[0-9A-F\\.a-z]+)

			final VFile imageFileInfo = publisherData.getRootNode().getImage(imageCode);
			newImagesMap.put(imageFileName, imageFileInfo);

			//on copie le début (avant le tag image)
			xmlOutput.append(xmlInput, offset, imageMatcher.start());

			final Dimension newImageDimension = computeNewImageDimension(getImageSize(imageFileInfo), imageMatcher.group(IMAGE_WIDTH_GROUP), imageMatcher.group(IMAGE_HEIGHT_GROUP));

			//puis les différentes info du tag image
			xmlOutput.append(imageMatcher.group(DUMMY1_GROUP));
			xmlOutput.append(imageMatcher.group(IMAGE_CODE_GROUP));
			xmlOutput.append(imageMatcher.group(DUMMY2_GROUP));
			xmlOutput.append(newImageDimension.width);
			xmlOutput.append(imageMatcher.group(DUMMY3_GROUP));
			xmlOutput.append(newImageDimension.height);
			xmlOutput.append(imageMatcher.group(DUMMY4_GROUP));
			xmlOutput.append(imageMatcher.group(IMAGE_FILENAME_GROUP));
			xmlOutput.append(imageMatcher.group(DUMMY5_GROUP));

			offset = imageMatcher.end();
		}
		xmlOutput.append(xmlInput, offset, xmlInput.length());

		return xmlOutput.toString();
	}

	private static Dimension computeNewImageDimension(final Dimension imageSize, final String imageWidthStr, final String imageHeightStr) {
		final double imageWidth = Double.parseDouble(imageWidthStr);
		final double imageHeight = Double.parseDouble(imageHeightStr);
		final double initialRatio = imageWidth / imageHeight;
		final double newRatio = imageSize.width / imageSize.height;
		final double resultWidth;
		final double resultHeight;

		if (Math.abs(initialRatio - newRatio) < .000001) { //FindBugs : préféré à initialRatio == newRatio
			resultWidth = imageWidth;
			resultHeight = imageHeight;
		} else if (newRatio > initialRatio) {
			//on garde la largeur
			resultWidth = imageWidth;
			resultHeight = Math.round(imageWidth * 1000 / newRatio) / 1000d; //round 3 chiffres après la virgule
		} else {
			//on garde la hauteur
			resultHeight = imageHeight;
			resultWidth = Math.round(imageHeight * 1000 * newRatio) / 1000d; //round 3 chiffres après la virgule
		}

		return new Dimension(resultWidth, resultHeight);
	}

	/**
	 * @return Map des nouveaux fichiers images, avec le path du fichier en clé, et le nouveau FileInfo en value.
	 */
	public Map<String, VFile> getNewImageMap() {
		return Collections.unmodifiableMap(newImagesMap);
	}

	private static Dimension getImageSize(final VFile imageFile) throws IOException {
		try (final InputStream is = imageFile.createInputStream()) {
			final BufferedImage image = ImageIO.read(is);
			return new Dimension(image.getWidth(), image.getHeight());
		}
	}

	private static class Dimension {
		final double width;
		final double height;

		public Dimension(final double width, final double height) {
			this.width = width;
			this.height = height;
		}
	}
}
