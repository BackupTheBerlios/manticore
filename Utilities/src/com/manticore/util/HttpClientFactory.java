package com.manticore.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;


public class HttpClientFactory {
	public final static long SECOND = 1000L;
	public final static int TIMEOUT = (int) (90 * SECOND);
	public final static Map HTTP_HEADERS = new HashMap();
	static {
		String ua = "";
		if (System.getProperty("os.name").matches("(?i).*Linux.*"))
			ua = "Mozilla/5.0 (X11; U; Linux i686; de-DE; rv:1.9.0.7) Gecko/2009021910 Firefox/3.0.7";
		else
			ua = "Mozilla/5.0 (Windows; U;Windows NT 5.1; de-DE; rv:1.9.0.7) Gecko/2009021910 Firefox/3.0.7";
		HTTP_HEADERS.put("User-Agent", ua);
		HTTP_HEADERS.put("Content-Type", "text/html; charset=utf-8");
		HTTP_HEADERS.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		HTTP_HEADERS.put("Accept-Language", "de-de,de;q=0.5");
		
		// http://hc.apache.org/httpclient-3.x/logging.html
//		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
//		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
//		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "DEBUG");
//		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "DEBUG");

	}

	public static DefaultHttpClient getClient() {
		// Create and initialize HTTP parameters
		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT);
		params.setParameter(CoreConnectionPNames.SO_TIMEOUT, TIMEOUT);

		ConnManagerParams.setMaxTotalConnections(params, 100);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		// Create and initialize scheme registry
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory
				.getSocketFactory(), 443));

		// Create an HttpClient with the ThreadSafeClientConnManager.
		// This connection manager must be used if more than one thread will
		// be using the HttpClient.
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
				schemeRegistry);
		DefaultHttpClient httpclient = new DefaultHttpClient(cm, params);

		// enrich headers
		httpclient.addRequestInterceptor(new HttpRequestInterceptor() {

			@Override
			public void process(final HttpRequest request,
					final HttpContext context) throws HttpException,
					IOException {
				for (Iterator it = HTTP_HEADERS.entrySet().iterator(); it
						.hasNext();) {
					Map.Entry entry = (Map.Entry) it.next();
					if (!request.containsHeader(entry.getKey().toString())) {
						request.addHeader(entry.getKey().toString(), entry
								.getValue().toString());
					}
				}
				if (!request.containsHeader("Accept-Encoding")) {
					request.addHeader("Accept-Encoding", "gzip");
				}
			}
		});

		httpclient.addResponseInterceptor(new HttpResponseInterceptor() {

			@Override
			public void process(final HttpResponse response,
					final HttpContext context) throws HttpException,
					IOException {
				HttpEntity entity = response.getEntity();
				Header ceheader = entity.getContentEncoding();
				if (ceheader != null) {
					HeaderElement[] codecs = ceheader.getElements();
					for (int i = 0; i < codecs.length; i++) {
						if ("gzip".equalsIgnoreCase(codecs[i].getName())) {
							response.setEntity(new GzipDecompressingEntity(
									response.getEntity()));
							return;
						}
					}
				}
			}

		});

		return httpclient;
	}

	// http://svn.apache.org/repos/asf/httpcomponents/httpclient/trunk/httpclient/src/examples/org/apache/http/examples/client/ClientGZipContentCompression.java
	static class GzipDecompressingEntity extends HttpEntityWrapper {

		public GzipDecompressingEntity(final HttpEntity entity) {
			super(entity);
		}

		@Override
		public InputStream getContent() throws IOException,
				IllegalStateException {

			// the wrapped entity's getContent() decides about repeatability
			InputStream wrappedin = super.wrappedEntity.getContent();

			return new GZIPInputStream(wrappedin);
		}

		@Override
		public long getContentLength() {
			// length of uncompressed content is not known yet
			return -1;
		}
	}
}
