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

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.Validateable;

import java.util.regex.Pattern;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public final class ValidationUtil
{

  /** Field description */
  private static final String REGEX_MAIL =
    "^[A-Za-z0-9][\\w.-]*@[A-Za-z0-9][\\w\\-\\.]*\\.[A-Za-z0-9][A-Za-z0-9-]+$";

  /** Field description */
  public static final String REGEX_NAME =
    "^[A-Za-z0-9\\.\\-_][A-Za-z0-9\\.\\-_@]*$";

  public static final String REGEX_REPOSITORYNAME = "(?!^\\.\\.$)(?!^\\.$)(?!.*[\\\\\\[\\]])(?!.*[.]git$)^[A-Za-z0-9\\.][A-Za-z0-9\\.\\-_]*$";

  /** Field description */
  private static final Pattern PATTERN_REPOSITORYNAME = Pattern.compile(REGEX_REPOSITORYNAME);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private ValidationUtil() {}

  //~--- get methods ----------------------------------------------------------

  /**
   *  Method description
   *
   *
   *  @param value
   *
   *  @return
   */
  public static boolean isFilenameValid(String value)
  {
    return Util.isNotEmpty(value) && isNotContaining(value, "/", "\\", ":");
  }

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  public static boolean isMailAddressValid(String value)
  {
    return Util.isNotEmpty(value) && value.matches(REGEX_MAIL);
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  public static boolean isNameValid(String name)
  {
    return Util.isNotEmpty(name) && name.matches(REGEX_NAME);
  }

  /**
   * Method description
   *
   *
   * @param value
   * @param notAllowedStrings
   *
   * @return
   */
  public static boolean isNotContaining(String value,
    String... notAllowedStrings)
  {
    boolean result = Util.isNotEmpty(value);

    if (result && (notAllowedStrings != null))
    {
      for (String nas : notAllowedStrings)
      {
        if (value.indexOf(nas) >= 0)
        {
          result = false;

          break;
        }
      }
    }

    return result;
  }

  /**
   * Returns {@code true} if the repository name is valid.
   *
   * @param name repository name
   * @since 1.9
   *
   * @return {@code true} if repository name is valid
   */
  public static boolean isRepositoryNameValid(String name) {
    return PATTERN_REPOSITORYNAME.matcher(name).matches();
  }

  /**
   * Method description
   *
   *
   * @param validateable
   *
   * @return
   */
  public static boolean isValid(Validateable validateable)
  {
    return (validateable != null) && validateable.isValid();
  }
}
