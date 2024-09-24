/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.net.ahc;


import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.net.HttpConnectionOptions;
import sonia.scm.net.HttpURLConnectionFactory;
import sonia.scm.trace.Span;
import sonia.scm.trace.Tracer;
import sonia.scm.util.HttpUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of the {@link AdvancedHttpClient}. The default
 * implementation uses {@link HttpURLConnection}.
 *
 * @since 1.46
 */
public class DefaultAdvancedHttpClient extends AdvancedHttpClient {

 
  private static final Logger LOG = LoggerFactory.getLogger(DefaultAdvancedHttpClient.class);

  private final HttpURLConnectionFactory connectionFactory;
  private final Tracer tracer;
  private final Set<ContentTransformer> contentTransformers;


  @Inject
  public DefaultAdvancedHttpClient(HttpURLConnectionFactory connectionFactory, Tracer tracer, Set<ContentTransformer> contentTransformers) {
    this.connectionFactory = connectionFactory;
    this.tracer = tracer;
    this.contentTransformers = contentTransformers;
  }

  @Override
  protected ContentTransformer createTransformer(Class<?> type, String contentType) {
    ContentTransformer responsible = null;

    for (ContentTransformer transformer : contentTransformers) {
      if (transformer.isResponsible(type, contentType)) {
        responsible = transformer;

        break;
      }
    }

    if (responsible == null) {
      throw new ContentTransformerNotFoundException(
        "could not find content transformer for content type ".concat(contentType)
      );
    }

    return responsible;
  }

  @Override
  protected AdvancedHttpResponse request(BaseHttpRequest<?> request) throws IOException {
    String spanKind = request.getSpanKind();
    if (Strings.isNullOrEmpty(spanKind)) {
      LOG.debug("execute request {} without tracing", request.getUrl());
      return doRequest(request);
    }
    return doRequestWithTracing(request);
  }

  @Nonnull
  private DefaultAdvancedHttpResponse doRequestWithTracing(BaseHttpRequest<?> request) throws IOException {
    try (Span span = tracer.span(request.getSpanKind())) {
      span.label("url", request.getUrl());
      span.label("method", request.getMethod());
      try {
        DefaultAdvancedHttpResponse response = doRequest(request);
        span.label("status", response.getStatus());
        if (isFailedRequest(request, response)) {
          span.failed();
        }
        return response;
      } catch (IOException ex) {
        span.label("exception", ex.getClass().getName());
        span.label("message", ex.getMessage());
        span.failed();
        throw ex;
      }
    }
  }

  private boolean isFailedRequest(BaseHttpRequest<?> request, AdvancedHttpResponse responseStatus) {
    if (Arrays.stream(request.getAcceptedStatus()).anyMatch(code -> code == responseStatus.getStatus())) {
      return false;
    }
     return !responseStatus.isSuccessful();
  }

  @Nonnull
  private DefaultAdvancedHttpResponse doRequest(BaseHttpRequest<?> request) throws IOException {
    HttpURLConnection connection = openConnection(request, new URL(request.getUrl()));
    connection.setRequestMethod(request.getMethod());

    Content content = null;
    if (request instanceof AdvancedHttpRequestWithBody) {
      AdvancedHttpRequestWithBody ahrwb = (AdvancedHttpRequestWithBody) request;

      content = ahrwb.getContent();

      if (content != null) {
        content.prepare(ahrwb);
      } else {
        request.header(HttpUtil.HEADER_CONTENT_LENGTH, "0");
      }
    } else {
      request.header(HttpUtil.HEADER_CONTENT_LENGTH, "0");
    }

    applyHeaders(request, connection);

    if (content != null) {
      applyContent(connection, content);
    }

    return new DefaultAdvancedHttpResponse(
      this, connection, connection.getResponseCode(), connection.getResponseMessage()
    );
  }

  private void applyContent(HttpURLConnection connection, Content content) throws IOException {
    connection.setDoOutput(true);
    try (OutputStream output = connection.getOutputStream()) {
      content.process(output);
    }
  }

  private void applyHeaders(BaseHttpRequest<?> request, HttpURLConnection connection) {
    Multimap<String, String> headers = request.getHeaders();
    for (Map.Entry<String, String> entry : headers.entries()) {
        connection.addRequestProperty(entry.getKey(), entry.getValue());
    }
  }

  private HttpURLConnection openConnection(BaseHttpRequest<?> request, URL url) throws IOException {
    return connectionFactory.create(url, createOptionsFromRequest(request));
  }

  private HttpConnectionOptions createOptionsFromRequest(BaseHttpRequest<?> request) {
    HttpConnectionOptions options = new HttpConnectionOptions();
    if (request.isDisableCertificateValidation()) {
      options.withDisableCertificateValidation();
    }
    if (request.isDisableHostnameValidation()) {
      options.withDisabledHostnameValidation();
    }
    if (request.isIgnoreProxySettings()) {
      options.withIgnoreProxySettings();
    }
    return options;
  }

}
