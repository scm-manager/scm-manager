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

  public static final String REGEX_REPOSITORYNAME = "(?!^\\.\\.$)(?!^\\.$)(?!.*[\\\\\\[\\]])^[A-Za-z0-9\\.][A-Za-z0-9\\.\\-_]*$";

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
