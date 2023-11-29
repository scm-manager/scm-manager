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
    
package sonia.scm.xml;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Converts a string to a string array and vice versa. The string is divided by
 * a comma.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public class XmlArrayStringAdapter extends XmlAdapter<String, String[]>
{

  /** separator char */
  private static final char SEPARATOR = ',';

  //~--- methods --------------------------------------------------------------

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
