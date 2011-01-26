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



package sonia.scm.plugin;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public enum PluginVersionType
{
  EARLY_ACESS("ea", 0, "early", "earlyaccess"), MILESTONE("M", 1, "milestone"),
  ALPHA("alpha", 2), BETA("beta", 3),
  RELEASE_CANDIDAT("RC", 4, "releasecandidate"), RELEASE(10);

  /**
   * Constructs ...
   *
   *
   * @param value
   */
  private PluginVersionType(int value)
  {
    this(null, value);
  }

  /**
   * Constructs ...
   *
   *
   *
   * @param id
   * @param value
   * @param aliases
   */
  private PluginVersionType(String id, int value, String... aliases)
  {
    this.id = id;
    this.value = value;
    this.aliases = aliases;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String[] getAliases()
  {
    return aliases;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getId()
  {
    return id;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<String> getNames()
  {
    List<String> names = new ArrayList<String>();

    if (id != null)
    {
      names.add(id);
    }

    if (aliases != null)
    {
      names.addAll(Arrays.asList(aliases));
    }

    return names;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getValue()
  {
    return value;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  public String[] aliases;

  /** Field description */
  private String id;

  /** Field description */
  private int value;
}
