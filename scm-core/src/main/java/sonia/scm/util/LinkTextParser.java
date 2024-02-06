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
    
package sonia.scm.util;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class LinkTextParser
{

  private static final Pattern REGEX_URL =
    Pattern.compile(
      "\\(?\\b((?:https?://|ftps?://|mailto:|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|])");

  private static final String REPLACE_URL =
    "<a target=\"_blank\" href=\"{1}\">{0}</a>";


  private LinkTextParser() {}

  public static String parseText(String content)
  {
    Matcher m = REGEX_URL.matcher(content);
    List<Token> tokens = new ArrayList<>();
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



  private static class Token
  {
    private String replacement;

    private String value;

    public Token(String value)
    {
      this.value = value;
    }

    public Token(String value, String replacement)
    {
      this.value = value;
      this.replacement = replacement;
    }


    public String getReplacement()
    {
      return replacement;
    }

    public String getValue()
    {
      return value;
    }
  }
}
