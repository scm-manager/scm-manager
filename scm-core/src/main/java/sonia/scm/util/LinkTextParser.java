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



package sonia.scm.util;

//~--- JDK imports ------------------------------------------------------------

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Sebastian Sdorra
 */
public class LinkTextParser
{

  /** Field description */
  private static final Pattern REGEX_URL =
    Pattern.compile(
        "\\(?\\b((?:https?://|ftps?://|mailto:|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|])");

  /** Field description */
  private static final String REPLACE_URL =
    "<a target=\"_blank\" href=\"{1}\">{0}</a>";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param content
   *
   * @return
   */
  public String parseText(String content)
  {
    Matcher m = REGEX_URL.matcher(content);
    List<Token> tokens = new ArrayList<Token>();
    int position = 0;
    String tokenContent = null;

    while (m.find())
    {
      int start = m.start();
      int end = m.end();
      String replacement = m.group();

      if (replacement.startsWith("www"))
      {
        replacement = "http://" + replacement;
      }

      tokenContent = content.substring(position, start);
      tokens.add(new Token(tokenContent));
      tokenContent = content.substring(start, end);
      tokens.add(new Token(tokenContent, replacement));
      position = end;
    }

    tokenContent = content.substring(position, content.length());
    tokens.add(new Token(tokenContent));

    StringBuilder buffer = new StringBuilder();

    for (Token token : tokens)
    {
      if (token.getReplacement() != null)
      {
        buffer.append(MessageFormat.format(REPLACE_URL, token.getValue(),
                                           token.getReplacement()));
      }
      else
      {
        buffer.append(token.getValue().replace("<", "&lt;").replace(">",
                "&gt;"));
      }
    }

    return buffer.toString();
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 11/11/06
   * @author         Enter your name here...    
   */
  private static class Token
  {

    /**
     * Constructs ...
     *
     *
     * @param value
     */
    public Token(String value)
    {
      this.value = value;
    }

    /**
     * Constructs ...
     *
     *
     * @param value
     * @param replacement
     */
    public Token(String value, String replacement)
    {
      this.value = value;
      this.replacement = replacement;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public String getReplacement()
    {
      return replacement;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getValue()
    {
      return value;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private String replacement;

    /** Field description */
    private String value;
  }
}
