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
import com.google.common.io.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


@RunWith(MockitoJUnitRunner.class)
public class FileContentTest
{


  @Test
  public void testPrepareRequest() throws IOException
  {
    FileContent content = create("abc");

    content.prepare(request);
    verify(request).contentLength(3l);
  }


  @Test
  public void testProcess() throws IOException
  {
    FileContent content = create("abc");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    content.process(baos);
    assertEquals("abc", baos.toString("UTF-8"));
  }

  private FileContent create(String value) throws IOException
  {
    File file = temp.newFile();

    Files.write("abc", file, Charsets.UTF_8);

    return new FileContent(file);
  }

  //~--- fields ---------------------------------------------------------------

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Mock
  private AdvancedHttpRequestWithBody request;
}
