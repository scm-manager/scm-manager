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

import com.google.common.base.Strings;
import sonia.scm.Validateable;

import java.util.regex.Pattern;

public final class ValidationUtil {

  private static final String REGEX_MAIL = "^[a-z0-9!#$%&'*+\\/=?^_`{|}~\" -]+(?:\\.[a-z0-9!#$%&'*+\\/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$";

  public static final String REGEX_NAME = "^(?:(?:[^:/?#;&=\\s@%\\\\][^:/?#;&=%\\\\]*[^:/?#;&=\\s%\\\\])|(?:[^:/?#;&=\\s@%\\\\]))$";

  public static final String REGEX_REPOSITORYNAME = "(?!^\\.\\.$)(?!^\\.$)(?!.*[\\\\\\[\\]])(?!.*[.]git$)^[A-Za-z0-9\\.][A-Za-z0-9\\.\\-_]*$";

  private static final Pattern PATTERN_REPOSITORYNAME = Pattern.compile(REGEX_REPOSITORYNAME);

  private ValidationUtil() {
  }

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

  public static boolean isMailAddressValid(String mail) {
    return Util.isNotEmpty(mail) && mail.toLowerCase().matches(REGEX_MAIL);
  }

  public static boolean isNameValid(String name) {
    return Util.isNotEmpty(name) && name.matches(REGEX_NAME) && !name.equals("..");
  }

  public static boolean isPasswordValid(String password) {
    String pw = Strings.nullToEmpty(password);
    return pw.length() >= 6 && pw.length() <= 1024;
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
