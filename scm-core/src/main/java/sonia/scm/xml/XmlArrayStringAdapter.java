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

package sonia.scm.xml;


import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Converts a string to a string array and vice versa. The string is divided by
 * a comma.
 *
 * @since 2.0.0
 */
public class XmlArrayStringAdapter extends XmlAdapter<String, String[]>
{

  /** separator char */
  private static final char SEPARATOR = ',';


  /**
   * Converts the array to a single string divided by commas.
   *
   *
   * @param array string array
   *
   * @return joined string
   */
  @Override
  public String marshal(String[] array)
  {
    String value = null;

    if (array != null)
    {
      value = Joiner.on(SEPARATOR).join(array);
    }

    return value;
  }

  /**
   * Splits the string to a array of strings.
   *
   *
   * @param rawString raw string
   *
   * @return string array
   */
  @Override
  public String[] unmarshal(String rawString)
  {
    String[] array = null;

    if (rawString != null)
    {
      array = Splitter.on(SEPARATOR).trimResults().splitToList(
        rawString).toArray(new String[0]);
    }

    return array;
  }
}
