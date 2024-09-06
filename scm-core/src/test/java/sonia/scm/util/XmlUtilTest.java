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
