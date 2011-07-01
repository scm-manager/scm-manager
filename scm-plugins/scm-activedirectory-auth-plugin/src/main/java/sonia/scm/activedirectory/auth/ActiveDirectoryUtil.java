/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.activedirectory.auth;

//~--- non-JDK imports --------------------------------------------------------

import com4j.Com4jObject;

import com4j.typelibs.activeDirectory.IADsGroup;
import com4j.typelibs.activeDirectory.IADsUser;
import com4j.typelibs.ado20.Field;
import com4j.typelibs.ado20.Fields;
import com4j.typelibs.ado20._Recordset;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author sdorra
 */
public class ActiveDirectoryUtil
{

  /**
   * Get the first String of a multivalue recordset item
   *
   *
   * @param rs
   * @param item
   *
   * @return the first item of a recordset
   */
  public static String getFirstString(_Recordset rs, String item)
  {
    String result = null;
    Object[] values = (Object[]) getObject(rs, item);

    if (Util.isNotEmpty(values))
    {
      Object value = values[0];

      if (value != null)
      {
        result = value.toString();
      }
    }

    return result;
  }

  /**
   * Fetch all groupnames of a user
   *
   *
   * @param usr
   *
   * @return
   */
  public static Collection<String> getGroups(IADsUser usr)
  {
    Set<String> groups = new TreeSet<String>();

    for (Com4jObject g : usr.groups())
    {
      IADsGroup grp = g.queryInterface(IADsGroup.class);

      // cut "CN=" and make that the role name
      String groupName = grp.name().substring(3);

      groups.add(groupName);
    }

    return groups;
  }

  /**
   * Method description
   *
   *
   * @param rs
   * @param item
   *
   * @return
   */
  public static Object getObject(_Recordset rs, String item)
  {
    Object value = null;
    Fields fields = rs.fields();

    if (fields != null)
    {
      Field field = fields.item(item);

      if (field != null)
      {
        value = field.value();
      }
    }

    return value;
  }

  /**
   * Method description
   *
   *
   * @param rs
   * @param item
   *
   * @return
   */
  public static String getString(_Recordset rs, String item)
  {
    String result = null;
    Object value = getObject(rs, item);

    if (value != null)
    {
      result = value.toString();
    }

    return result;
  }
}
