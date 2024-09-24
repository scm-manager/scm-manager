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
