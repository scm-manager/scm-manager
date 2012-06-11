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


package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;

import org.apache.commons.lang.StringEscapeUtils;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.15
 */
public class EscapeUtil
{

  /**
   * Method description
   *
   *
   * @param result
   */
  public static void escape(BrowserResult result)
  {
    result.setBranch(escape(result.getBranch()));
    result.setTag(escape(result.getTag()));

    for (FileObject fo : result)
    {
      escape(fo);
    }
  }

  /**
   * Method description
   *
   *
   * @param fo
   */
  public static void escape(FileObject fo)
  {
    fo.setDescription(escape(fo.getDescription()));
    fo.setName(fo.getName());
    fo.setPath(fo.getPath());
  }

  /**
   * Method description
   *
   *
   * @param changeset
   */
  public static void escape(Changeset changeset)
  {
    changeset.setDescription(escape(changeset.getDescription()));

    Person person = changeset.getAuthor();

    if (person != null)
    {
      person.setName(escape(person.getName()));
      person.setMail(escape(person.getMail()));
    }

    changeset.setBranches(escapeList(changeset.getBranches()));
    changeset.setTags(escapeList(changeset.getTags()));
  }

  /**
   * Method description
   *
   *
   * @param result
   */
  public static void escape(ChangesetPagingResult result)
  {
    for (Changeset c : result)
    {
      escape(c);
    }
  }

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  public static String escape(String value)
  {
    return StringEscapeUtils.escapeHtml(value);
  }

  /**
   * Method description
   *
   *
   * @param values
   *
   * @return
   */
  public static List<String> escapeList(List<String> values)
  {
    if (Util.isNotEmpty(values))
    {
      List<String> newList = Lists.newArrayList();

      for (String v : values)
      {
        newList.add(StringEscapeUtils.escapeHtml(v));
      }

      values = newList;
    }

    return values;
  }
}
