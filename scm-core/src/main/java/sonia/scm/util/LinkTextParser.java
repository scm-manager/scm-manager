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
