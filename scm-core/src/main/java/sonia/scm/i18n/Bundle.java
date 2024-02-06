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

package sonia.scm.i18n;


import sonia.scm.util.ClassLoaders;
import sonia.scm.util.Util;

import java.text.MessageFormat;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This class is a wrapper for {@link ResourceBundle}, it applies some missing
 * format options missing in {@link ResourceBundle}.
 *
 * @since 1.15
 */
public class Bundle
{
  private final ResourceBundle bundle;

  private static final String SEPARATOR = System.getProperty("line.separator");

  private Bundle(ResourceBundle bundle)
  {
    this.bundle = bundle;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Creates a new bundle instance
   *
   * @param path path to the properties file
   *
   * @return new bundle instance
   */
  public static Bundle getBundle(String path)
  {
    return getBundle(path, null, null);
  }

  /**
   * Creates a new bundle instance
   *
   * @param path path to the properties file
   * @param locale locale for the properties file
   *
   * @return new bundle instance
   */
  public static Bundle getBundle(String path, Locale locale)
  {
    return getBundle(path, locale, null);
  }

  /**
   * Creates a new bundle instance
   *
   * @param path path to the properties file
   * @param locale locale for the properties file
   * @param classLoader classLoader to load
   *
   * @return new bundle instance
   *
   * @since 1.37
   */
  public static Bundle getBundle(String path, Locale locale,
    ClassLoader classLoader)
  {
    if (locale == null)
    {
      locale = Locale.ENGLISH;
    }

    if (classLoader == null)
    {
      classLoader = ClassLoaders.getContextClassLoader(Bundle.class);
    }

    return new Bundle(ResourceBundle.getBundle(path, locale, classLoader));
  }

  /**
   * This method returns the same value as
   * {@link #getString(java.lang.String, java.lang.Object[])}
   * with a line separator at the end.
   *
   * @param key key in the properties file
   * @param args format arguments
   *
   * @return formatted message
   */
  public String getLine(String key, Object... args)
  {
    return getString(key, args).concat(SEPARATOR);
  }

  /**
   * Returns the value of the key, formatted with {@link MessageFormat}
   *
   *
   * @param key key in the properties file
   * @param args format arguments
   *
   * @return formatted message
   */
  public String getString(String key, Object... args)
  {
    String msg = bundle.getString(key);

    if (Util.isNotEmpty(args))
    {
      msg = MessageFormat.format(msg, args);
    }

    return msg;
  }

  /**
   * Returns the value of the key, formatted with {@link MessageFormat} or null
   * if the key is not present in the bundle.
   *
   *
   * @param key key in the properties file
   * @param args format arguments
   *
   * @return formatted message or null
   *
   * @since 1.37
   */
  public String getStringIfPresent(String key, Object... args)
  {
    String msg = null;

    try
    {
      msg = getString(key, args);
    }
    catch (MissingResourceException ex) {}

    return msg;
  }
}
