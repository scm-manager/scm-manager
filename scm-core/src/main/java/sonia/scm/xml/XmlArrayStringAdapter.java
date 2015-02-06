/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.xml;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.adapters.XmlAdapter;

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
