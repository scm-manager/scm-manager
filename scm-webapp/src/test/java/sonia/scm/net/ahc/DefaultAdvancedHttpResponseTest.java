/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.net.ahc;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteSource;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.net.HttpURLConnection;

import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultAdvancedHttpResponseTest
{

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

    AdvancedHttpResponse response = new DefaultAdvancedHttpResponse(connection,
                                      200, "OK");
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
  public void testContentAsByteSourceWithFailedRequest() throws IOException
  {
    ByteArrayInputStream bais =
      new ByteArrayInputStream("test".getBytes(Charsets.UTF_8));

    when(connection.getInputStream()).thenThrow(IOException.class);
    when(connection.getErrorStream()).thenReturn(bais);

    AdvancedHttpResponse response = new DefaultAdvancedHttpResponse(connection,
                                      404, "NOT FOUND");
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

    AdvancedHttpResponse response = new DefaultAdvancedHttpResponse(connection,
                                      200, "OK");
    Multimap<String, String> headers = response.getHeaders();

    assertThat(headers.get("Test"), contains("One", "Two"));
    assertTrue(headers.get("Test-2").isEmpty());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Mock
  private HttpURLConnection connection;
}
