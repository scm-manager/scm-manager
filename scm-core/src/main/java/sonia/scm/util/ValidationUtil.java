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

import com.google.common.base.Strings;
import sonia.scm.Validateable;

import java.util.regex.Pattern;

public final class ValidationUtil {

  private static final String REGEX_MAIL = "^[A-Za-z0-9][\\w.-]*@[A-Za-z0-9][\\w\\-\\.]*\\.[A-Za-z0-9][A-Za-z0-9-]+$";

  public static final String REGEX_NAME = "^(?:(?:[^:/?#;&=\\s@%\\\\][^:/?#;&=%\\\\]*[^:/?#;&=\\s%\\\\])|(?:[^:/?#;&=\\s@%\\\\]))$";

  public static final String REGEX_REPOSITORYNAME = "(?!^\\.\\.$)(?!^\\.$)(?!.*[\\\\\\[\\]])(?!.*[.]git$)^[A-Za-z0-9\\.][A-Za-z0-9\\.\\-_]*$";

  private static final Pattern PATTERN_REPOSITORYNAME = Pattern.compile(REGEX_REPOSITORYNAME);

  private ValidationUtil() {
  }

  /**
   * Returns {@code true} if the filename is valid.
   *
   * @param filename filename to be validated
   * @return {@code true} if filename is valid
   */
  public static boolean isFilenameValid(String filename) {
    return Util.isNotEmpty(filename) && isNotContaining(filename, "/", "\\", ":");
  }

  /**
   *  Checks if the path is a valid path and does not enable path traversal
   *
   *  @param path path to be validated
   *
   *  @return {@code true} if path is valid else false
   */
  public static boolean isPathValid(String path)
  {
    return !path.equals(".")
      && !path.contains("../")
      && !path.contains("//")
      && !path.contains("\\")
      && !path.equals("..");
  }

  /**
   * Returns {@code true} if the mail is valid.
   *
   * @param mail email-address to be validated
   * @return {@code true} if mail is valid
   */
  public static boolean isMailAddressValid(String mail) {
    return Util.isNotEmpty(mail) && mail.matches(REGEX_MAIL);
  }

  /**
   * Returns {@code true} if the name is valid.
   *
   * @param name name to be validated
   * @return {@code true} if name is valid
   */
  public static boolean isNameValid(String name) {
    return Util.isNotEmpty(name) && name.matches(REGEX_NAME) && !name.equals("..");
  }

  /**
   * Returns {@code true} if the user password is valid.
   *
   * @param password password to be validated
   * @param isExternal whether user is external
   * @return {@code true} if password is valid
   */
  public static boolean isPasswordValid(String password, Boolean isExternal) {
    String pw = Strings.nullToEmpty(password);
    if (Boolean.TRUE.equals(isExternal) || isPasswordEncrypted(pw)) {
      return true;
    }
    return pw.length() >= 6 && pw.length() <= 1024;
  }

  private static boolean isPasswordEncrypted(String pw) {
    return pw.startsWith("$shiro1$SHA-512$8192$$");
  }

  /**
   * Returns {@code true} if the object is valid.
   *
   * @param value             value to be checked
   * @param notAllowedStrings one or more strings which should not be included in value
   * @return {@code true} if string has no not allowed strings else false
   */
  public static boolean isNotContaining(String value, String... notAllowedStrings) {
    boolean result = Util.isNotEmpty(value);

    if (result && (notAllowedStrings != null)) {
      for (String nas : notAllowedStrings) {
        if (value.contains(nas)) {
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
   * @return {@code true} if repository name is valid
   * @since 1.9
   */
  public static boolean isRepositoryNameValid(String name) {
    return PATTERN_REPOSITORYNAME.matcher(name).matches();
  }

  /**
   * Returns {@code true} if the object is valid.
   *
   * @param validatable object to be validated
   * @return {@code true} if object is valid
   */
  public static boolean isValid(Validateable validatable) {
    return (validatable != null) && validatable.isValid();
  }
}
