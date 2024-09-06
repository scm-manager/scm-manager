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
import org.junit.Test;
import static org.junit.Assert.*;


public class StringContentTest {


  @Test
  public void testStringContent()
  {
    StringContent sc = new StringContent("abc", Charsets.UTF_8);
    assertEquals("abc", new String(sc.getData()));
  }
  
  @Test
  public void testStringContentWithCharset()
  {
    StringContent sc = new StringContent("üäö", Charsets.ISO_8859_1);
    assertEquals("üäö", new String(sc.getData(), Charsets.ISO_8859_1));
  }

}
