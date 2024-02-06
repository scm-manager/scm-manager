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
    
package sonia.scm.util;


import com.google.common.collect.Multimap;
import com.google.common.io.Resources;

import org.junit.Test;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;


public class XmlUtilTest
{


  @Test
  public void testValues() throws IOException
  {
    URL url = Resources.getResource("sonia/scm/util/xmlutil.01.xml");

    try (InputStream stream = url.openStream())
    {
      Multimap<String, String> map = XmlUtil.values(stream, "entry01",
                                       "entry02", "entry03", "entry04");

      assertThat(map.get("entry01"), containsInAnyOrder("value01"));
      assertThat(map.get("entry02"),
        containsInAnyOrder("value02", "value02x2"));
      assertThat(map.get("entry03"), containsInAnyOrder("value03"));
      assertTrue(map.get("entry04").isEmpty());
    }
  }
}
