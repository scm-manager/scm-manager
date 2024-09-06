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
import com.google.common.io.ByteSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ByteSourceContentTest {

  @Mock
  private AdvancedHttpRequestWithBody request;

  @Test
  public void testPrepareRequest() throws IOException
  {
    ByteSource source = ByteSource.wrap("abc".getBytes(Charsets.UTF_8));
    ByteSourceContent content = new ByteSourceContent(source);
    content.prepare(request);
    verify(request).contentLength(3l);
  }
  
  @Test
  public void testProcess() throws IOException{
    ByteSource source = ByteSource.wrap("abc".getBytes(Charsets.UTF_8));
    ByteSourceContent content = new ByteSourceContent(source);
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    content.process(baos);
    assertEquals("abc", baos.toString("UTF-8"));
  }

}
