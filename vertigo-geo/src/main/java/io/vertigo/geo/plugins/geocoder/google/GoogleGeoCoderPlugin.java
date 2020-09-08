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
package io.vertigo.geo.plugins.geocoder.google;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.param.ParamValue;
import io.vertigo.geo.geocoder.GeoLocation;
import io.vertigo.geo.impl.geocoder.GeoCoderPlugin;

/**
 * @author spoitrenaud
 *
 */
public final class GoogleGeoCoderPlugin implements GeoCoderPlugin {
	// Début de la requête http
	private static final String GEOCODE_REQUEST_PREFIX = "https://maps.googleapis.com/maps/api/geocode/xml";
	// Expression XPath permettant de récupérer la latitude, la longitude et
	// l'adresse formatée de
	// l'adresse à géolocaliser
	private static final String XPATH_LATITUDE = "//result//geometry//location//lat";
	private static final String XPATH_LONGITUDE = "//result//geometry//location//lng";
	private static final String XPATH_ADDRESSES = "//address_component";
	private static final String XPATH_STATUS = "//status";
	private final Proxy proxy;
	private final XPathFactory xPathFactory = XPathFactory.newInstance();

	//	/**
	//	 * Méthode d''initialisation de l'API.
	//	 */
	@Inject
	public GoogleGeoCoderPlugin(
			final @ParamValue("proxyHost") Optional<String> proxyHostOpt,
			@ParamValue("proxyPort") final Optional<String> proxyPortOpt) {
		Assertion.check()
				.isNotNull(proxyHostOpt)
				.isNotNull(proxyPortOpt)
				.isTrue((proxyHostOpt.isPresent() && proxyPortOpt.isPresent()) || (proxyHostOpt.isEmpty() && proxyPortOpt.isEmpty()),
						"les deux paramètres host et port doivent être tous les deux remplis ou vides");
		//-----
		proxy = proxyHostOpt
				.map(proxyHost -> new Proxy(Proxy.Type.HTTP,
						new InetSocketAddress(proxyHost, Integer.parseInt(proxyPortOpt.get()))))
				.orElse(Proxy.NO_PROXY);
	}

	/**
	 * Récupération d'une connexion.
	 *
	 * @param url type URL
	 * @return type Document contenant les résultats de la requête
	 */
	private HttpURLConnection createConnection(final URL url) {
		try {
			return doCreateConnection(url);
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Erreur de connexion au service (HTTP)");
		}
	}

	private HttpURLConnection doCreateConnection(final URL url) throws IOException {
		Assertion.check().isNotNull(url);
		//-----
		final HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
		connection.setDoOutput(true);
		return connection;
	}

	/**
	 * Méthode de connexion au service Google.
	 *
	 * @param address Chaîne de caractères contenant l'adresse à geocoder
	 * @return Document
	 */
	private Document geoCode(final String address) {
		Assertion.check().isNotNull(address);
		//-----
		final String urlString;
		try {
			urlString = GEOCODE_REQUEST_PREFIX + "?address=" + URLEncoder.encode(address, StandardCharsets.UTF_8.name()) + "&sensor=false";
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException("Erreur lors de l'encodage de l'adresse", e);
		}

		final URL url;
		try {
			url = new URL(urlString);
		} catch (final MalformedURLException e) {
			throw WrappedException.wrap(e, "Erreur lors de la creation de l'URL");
		}

		final HttpURLConnection connection = createConnection(url);
		try {
			// Connexion et récupération des résultats
			connection.connect();
			final InputSource geocoderResultInputSource = new InputSource(connection.getInputStream());

			// Lecture des résultats sous forme XML
			final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			return documentBuilderFactory.newDocumentBuilder().parse(geocoderResultInputSource);
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Erreur de connexion au service");
		} catch (final SAXException e) {
			throw WrappedException.wrap(e, "Erreur lors de la récupération des résultats de la requête");
		} catch (final ParserConfigurationException e) {
			throw WrappedException.wrap(e, "Erreur de configuration du parseur XML");
		} finally {
			connection.disconnect();
		}
	}

	/**
	 * Parseur XML avec l'expression XPath.
	 *
	 * @param xml : le document XML récupéré depuis Google Geocoder
	 * @param xPathString : l'expression XPath permettant de parser le XML
	 * @return NodeList contenant les données du fichier XML
	 */
	private NodeList findNodes(final Document xml, final String xPathString) {
		Assertion.check()
				.isNotNull(xml)
				.isNotBlank(xPathString);
		//-----
		final XPath xpath = xPathFactory.newXPath();

		try {
			return (NodeList) xpath.evaluate(xPathString, xml, XPathConstants.NODESET);
		} catch (final XPathExpressionException ex) {
			throw WrappedException.wrap(ex, "Erreur lors du Parsing XML");
		}
	}

	/**
	 * Parseur XML avec l'expression XPath.
	 *
	 * @param xml : le document XML récupéré depuis Google Geocoder
	 * @param xPathString : l'expression XPath permettant de parser le XML
	 * @return Node contenant les données du fichier XML
	 */
	private Node findNode(final Document xml, final String xPathString) {
		Assertion.check()
				.isNotNull(xml)
				.isNotBlank(xPathString);
		//-----
		final XPath xpath = xPathFactory.newXPath();
		try {
			return (Node) xpath.evaluate(xPathString, xml, XPathConstants.NODE);
		} catch (final XPathExpressionException ex) {
			throw WrappedException.wrap(ex, "Erreur lors du Parsing XML");
		}
	}

	public static String toString(final Document doc) {
		try {
			final StringWriter sw = new StringWriter();
			final TransformerFactory tf = TransformerFactory.newInstance();
			tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			final Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString();
		} catch (final Exception ex) {
			throw WrappedException.wrap(ex, "Error converting to String");
		}
	}

	/** {@inheritDoc} */
	@Override
	public GeoLocation findLocation(final String address) {
		Assertion.check().isNotNull(address);
		//-----
		final Document geocoderResultDocument = geoCode(address);
		if (geocoderResultDocument == null) {
			throw new RuntimeException("Pas de réponse du service");
		}
		//-----
		// 0- Vérification du status
		final Node StatusNode = findNode(geocoderResultDocument, XPATH_STATUS);
		if (!"OK".equals(StatusNode.getTextContent().trim())) {
			return GeoLocation.UNDEFINED;
		}

		// 1- Parsing du XML
		final Node latitudeNode = findNode(geocoderResultDocument, XPATH_LATITUDE);
		final Node longitudeNode = findNode(geocoderResultDocument, XPATH_LONGITUDE);
		final NodeList addressNodes = findNodes(geocoderResultDocument, XPATH_ADDRESSES);
		//-----
		// 2- Typage des données
		final double latitude = Double.parseDouble(latitudeNode.getTextContent().trim());
		final double longitude = Double.parseDouble(longitudeNode.getTextContent().trim());
		//-----
		// 2- Cas des adresses dites "political"
		//		<address_component>
		//			<long_name>France</long_name>
		//			<short_name>FR</short_name>
		//			<type>country</type>
		//			<type>political</type>
		//		</address_component>
		//-----
		String countryCode = null;
		String level1 = null;
		String level2 = null;
		String locality = null;
		for (int i = 0; i < addressNodes.getLength(); i++) {
			final Node addressNode = addressNodes.item(i);
			String shortName = null;
			boolean isCountry = false;
			boolean isAdministrative_area_level_1 = false;
			boolean isAdministrative_area_level_2 = false;
			boolean isLocality = false;

			for (int j = 0; j < addressNode.getChildNodes().getLength(); j++) {
				final Node node = addressNode.getChildNodes().item(j);
				if ("short_name".equals(node.getNodeName())) {
					shortName = node.getTextContent().trim();
				} else if ("type".equals(node.getNodeName())) {
					if ("country".equals(node.getTextContent())) {
						isCountry = true;
					} else if ("administrative_area_level_1".equals(node.getTextContent())) {
						isAdministrative_area_level_1 = true;
					} else if ("administrative_area_level_2".equals(node.getTextContent())) {
						isAdministrative_area_level_2 = true;
					} else if ("locality".equals(node.getTextContent())) {
						isLocality = true;
					}
				}
			}
			if (isCountry) {
				countryCode = shortName;
			} else if (isAdministrative_area_level_1) {
				level1 = shortName;
			} else if (isAdministrative_area_level_2) {
				level2 = shortName;
			} else if (isLocality) {
				locality = shortName;
			}
		}
		//-----
		// 3- Création du résultat :  GeoLocation
		System.out.println(">>address : " + address);
		System.out.println("		>>level1 : " + level1);
		System.out.println("		>>level2 : " + level2);

		return new GeoLocation(latitude, longitude, countryCode, level1, level2, locality);
	}
}
