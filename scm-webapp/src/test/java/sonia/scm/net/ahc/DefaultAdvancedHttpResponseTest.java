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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteSource;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import sonia.scm.config.ScmConfiguration;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.net.HttpURLConnection;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import sonia.scm.net.SSLContextProvider;
import sonia.scm.trace.Tracer;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultAdvancedHttpResponseTest
{

  private DefaultAdvancedHttpClient client;

  @Before
  public void setUpClient() {
    client = new DefaultAdvancedHttpClient(new ScmConfiguration(), tracer, new HashSet<>(), new SSLContextProvider());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testContentAsByteSource() throws IOException
  {
    ByteArrayInputStream bais =
      new ByteArrayInputStream("test".getBytes(Charsets.UTF_8));

    when(connection.getInputStream()).thenReturn(bais);

    AdvancedHttpResponse response = new DefaultAdvancedHttpResponse(client,
                                      connection, 200, "OK");
    ByteSource content = response.contentAsByteSource();

    assertEquals("test", content.asCharSource(Charsets.UTF_8).read());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testContentAsByteSourceWithFailedRequest() throws IOException
  {
    ByteArrayInputStream bais =
      new ByteArrayInputStream("test".getBytes(Charsets.UTF_8));

    when(connection.getInputStream()).thenThrow(IOException.class);
    when(connection.getErrorStream()).thenReturn(bais);

    AdvancedHttpResponse response = new DefaultAdvancedHttpResponse(client,
                                      connection, 404, "NOT FOUND");
    ByteSource content = response.contentAsByteSource();

    assertEquals("test", content.asCharSource(Charsets.UTF_8).read());
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetHeaders()
  {
    LinkedHashMap<String, List<String>> map = Maps.newLinkedHashMap();
    List<String> test = Lists.newArrayList("One", "Two");

    map.put("Test", test);
    when(connection.getHeaderFields()).thenReturn(map);

    AdvancedHttpResponse response = new DefaultAdvancedHttpResponse(client,
                                      connection, 200, "OK");
    Multimap<String, String> headers = response.getHeaders();

    assertThat(headers.get("Test"), Matchers.contains("One", "Two"));
    assertTrue(headers.get("Test-2").isEmpty());
  }

  /** Field description */
  @Mock
  private HttpURLConnection connection;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Tracer tracer;
}
