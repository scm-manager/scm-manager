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

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class AdvancedHttpClientTest {

  @Mock(answer = Answers.CALLS_REAL_METHODS)
  private AdvancedHttpClient client;

  private static final String URL = "https://www.scm-manager.org";
  
  @Test
  public void testGet()
  {
    AdvancedHttpRequest request = client.get(URL);
    assertEquals(URL, request.getUrl());
    assertEquals(HttpMethod.GET, request.getMethod());
  }
  
  @Test
  public void testDelete()
  {
    AdvancedHttpRequestWithBody request = client.delete(URL);
    assertEquals(URL, request.getUrl());
    assertEquals(HttpMethod.DELETE, request.getMethod());
  }
  
  @Test
  public void testPut()
  {
    AdvancedHttpRequestWithBody request = client.put(URL);
    assertEquals(URL, request.getUrl());
    assertEquals(HttpMethod.PUT, request.getMethod());
  }
  
  @Test
  public void testPost()
  {
    AdvancedHttpRequestWithBody request = client.post(URL);
    assertEquals(URL, request.getUrl());
    assertEquals(HttpMethod.POST, request.getMethod());
  }
  
  @Test
  public void testOptions()
  {
    AdvancedHttpRequestWithBody request = client.options(URL);
    assertEquals(URL, request.getUrl());
    assertEquals(HttpMethod.OPTIONS, request.getMethod());
  }
  
  @Test
  public void testHead()
  {
    AdvancedHttpRequest request = client.head(URL);
    assertEquals(URL, request.getUrl());
    assertEquals(HttpMethod.HEAD, request.getMethod());
  }
  
  @Test
  public void testMethod()
  {
    AdvancedHttpRequestWithBody request = client.method("PROPFIND", URL);
    assertEquals(URL, request.getUrl());
    assertEquals("PROPFIND", request.getMethod());
  }
}
