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


import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteSource;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.net.HttpURLConnectionFactory;
import sonia.scm.trace.Tracer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DefaultAdvancedHttpResponseTest {

  @Mock
  private HttpURLConnection connection;

  private DefaultAdvancedHttpClient client;

  @BeforeEach
  void setUpClient() {
    client = new DefaultAdvancedHttpClient(mock(HttpURLConnectionFactory.class), mock(Tracer.class), Collections.emptySet());
  }

  @Test
  void shouldReturnContentAsByteSource() throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream("test".getBytes(Charsets.UTF_8));
    when(connection.getInputStream()).thenReturn(bais);

    AdvancedHttpResponse response = new DefaultAdvancedHttpResponse(client, connection, 200, "OK");
    ByteSource content = response.contentAsByteSource();

    assertThat(content.asCharSource(Charsets.UTF_8).read()).isEqualTo("test");
  }

  @Test
  void shouldReturnContentAsByteSourceEvenForFailedRequests() throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream("test".getBytes(Charsets.UTF_8));
    when(connection.getInputStream()).thenThrow(IOException.class);
    when(connection.getErrorStream()).thenReturn(bais);

    AdvancedHttpResponse response = new DefaultAdvancedHttpResponse(client, connection, 404, "NOT FOUND");
    ByteSource content = response.contentAsByteSource();

    assertThat(content.asCharSource(Charsets.UTF_8).read()).isEqualTo("test");
  }

  @Test
  void shouldReturnHeaders() {
    LinkedHashMap<String, List<String>> map = Maps.newLinkedHashMap();
    List<String> test = Lists.newArrayList("One", "Two");

    map.put("Test", test);
    when(connection.getHeaderFields()).thenReturn(map);

    AdvancedHttpResponse response = new DefaultAdvancedHttpResponse(client,
                                      connection, 200, "OK");
    Multimap<String, String> headers = response.getHeaders();

    assertThat(headers.get("Test")).containsOnly("One", "Two");
    assertThat(headers.get("Test-2")).isEmpty();
  }
}
