/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
