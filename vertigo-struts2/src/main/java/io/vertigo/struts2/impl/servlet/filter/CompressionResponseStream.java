package io.vertigo.struts2.impl.servlet.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Implémentation de ServletOutputStream qui fonctionne avec le CompressionServletResponseWrapper.
 * @author Emeric Vernat
 */
class CompressionResponseStream extends ServletOutputStream {
	private final int compressionThreshold;
	private final HttpServletResponse response;
	private OutputStream stream;

	/**
	 * Construit un servlet output stream associé avec la réponse sp�cifi�e.
	 * @param response javax.servlet.http.HttpServletResponse
	 * @param compressionThreshold int
	 */
	CompressionResponseStream(final HttpServletResponse response, final int compressionThreshold) {
		super();
		this.response = response;
		this.compressionThreshold = compressionThreshold;
		stream = new ByteArrayOutputStream(compressionThreshold);
	}

	/**
	 * Ferme cet output stream (et flushe les données bufferis�es).
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public void close() throws IOException {
		if (stream instanceof ByteArrayOutputStream) {
			final byte[] bytes = ((ByteArrayOutputStream) stream).toByteArray();
			response.getOutputStream().write(bytes);
			stream = response.getOutputStream();
		}
		stream.close();
	}

	/**
	 * Flushe les données bufferis�es de cet output stream.
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public void flush() throws IOException {
		stream.flush();
	}

	private void checkBufferSize(final int length) throws IOException {
		// check if we are buffering too large of a file
		if (stream instanceof ByteArrayOutputStream) {
			final ByteArrayOutputStream baos = (ByteArrayOutputStream) stream;
			if (baos.size() + length > compressionThreshold) {
				// files too large to keep in memory are sent to the client
				flushToGZIP();
			}
		}
	}

	private void flushToGZIP() throws IOException {
		if (stream instanceof ByteArrayOutputStream) {
			// indication de compression
			response.addHeader("Content-Encoding", "gzip");
			response.addHeader("Vary", "Accept-Encoding");

			// make new gzip stream using the response output stream (content-encoding is in constructor)
			final GZIPOutputStream gzipstream = new GZIPOutputStream(response.getOutputStream(), compressionThreshold);
			// get existing bytes
			final byte[] bytes = ((ByteArrayOutputStream) stream).toByteArray();
			gzipstream.write(bytes);
			// we are no longer buffering, send content via gzipstream
			stream = gzipstream;
		}
	}

	/**
	 * Ecrit l'octet sp�cifi� dans l'output stream
	 * @param i int
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public void write(final int i) throws IOException {
		// make sure we aren't over the buffer's limit
		checkBufferSize(1);
		// write the byte to the temporary output
		stream.write(i);
	}

	/**
	 * Ecrit les octets sp�cifi�s dans l'output stream.
	 * @param bytes bytes[]
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public void write(final byte[] bytes) throws IOException {
		write(bytes, 0, bytes.length);
	}

	/**
	 * Ecrit <code>len</code> octets du tableau d'octets sp�cifi�s, en commençant à la position sp�cifi�e,
	 * dans l'output stream.
	 * @param bytes bytes[]
	 * @param off int
	 * @param len int
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public void write(final byte[] bytes, final int off, final int len) throws IOException {
		if (len == 0) {
			return;
		}

		// make sure we aren't over the buffer's limit
		checkBufferSize(len);
		// write the content to the buffer
		stream.write(bytes, off, len);
	}
}
