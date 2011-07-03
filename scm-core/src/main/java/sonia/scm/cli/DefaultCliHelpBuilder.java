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



package sonia.scm.cli;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultCliHelpBuilder implements CliHelpBuilder
{

  /**
   * Constructs ...
   *
   */
  public DefaultCliHelpBuilder() {}

  /**
   * Constructs ...
   *
   *
   * @param prefix
   * @param suffix
   */
  public DefaultCliHelpBuilder(String prefix, String suffix)
  {
    this.prefix = prefix;
    this.suffix = suffix;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param arguments
   *
   * @return
   */
  @Override
  public String createHelp(List<Argument> arguments)
  {
    String s = System.getProperty("line.separator");
    int paramLength = 20;
    List<HelpLine> list = new ArrayList<HelpLine>();

    if (arguments != null)
    {
      for (Argument argument : arguments)
      {
        HelpLine line = new HelpLine();
        String name = argument.value();
        String longName = argument.longName();
        String description = argument.description();
        StringBuilder result = new StringBuilder("-");

        result.append(name);

        if (longName.length() > 0)
        {
          result.append(",--").append(longName);
        }

        line.parameter = result.toString();

        if (line.parameter.length() > paramLength)
        {
          paramLength = line.parameter.length();
        }

        if (description.length() > 0)
        {
          line.description = description;
        }

        list.add(line);
      }
    }

    paramLength += 2;
    Collections.sort(list);

    StringBuilder result = new StringBuilder();

    if ((prefix != null) && (prefix.trim().length() > 0))
    {
      result.append(prefix).append(s);
    }

    for (HelpLine line : list)
    {
      int length = line.parameter.length();

      result.append(line.parameter);

      for (int i = length; i < paramLength; i++)
      {
        result.append(" ");
      }

      result.append(line.description).append(s);
    }

    if ((suffix != null) && (suffix.trim().length() > 0))
    {
      result.append(suffix).append(s);
    }

    return result.toString();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getPrefix()
  {
    return prefix;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getSuffix()
  {
    return suffix;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param prefix
   */
  public void setPrefix(String prefix)
  {
    this.prefix = prefix;
  }

  /**
   * Method description
   *
   *
   * @param suffix
   */
  public void setSuffix(String suffix)
  {
    this.suffix = suffix;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 10/03/06
   * @author         Enter your name here...
   */
  private static class HelpLine implements Comparable<HelpLine>
  {

    /**
     * Method description
     *
     *
     * @param o
     *
     * @return
     */
    @Override
    public int compareTo(HelpLine o)
    {
      return parameter.compareTo(o.parameter);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private String description;

    /** Field description */
    private String parameter;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String prefix;

  /** Field description */
  private String suffix;
}
