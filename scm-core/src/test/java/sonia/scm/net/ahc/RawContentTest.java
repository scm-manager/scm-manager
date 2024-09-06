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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class RawContentTest {


  @Mock
  private AdvancedHttpRequestWithBody request;

  @Test
  public void testPrepareRequest()
  {
    RawContent raw = new RawContent("abc".getBytes(Charsets.UTF_8));
    raw.prepare(request);
    verify(request).contentLength(3);
  }
  
  @Test
  public void testProcess() throws IOException
  {
    RawContent raw = new RawContent("abc".getBytes(Charsets.UTF_8));
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    raw.process(baos);
    assertEquals("abc", baos.toString("UTF-8"));
  }

}
