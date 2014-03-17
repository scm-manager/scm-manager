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



package sonia.scm.i18n;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.ClassLoaders;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.text.MessageFormat;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This class is a wrapper for {@link ResourceBundle}, it applies some missing
 * format options missing in {@link ResourceBundle}.
 *
 * @author Sebastian Sdorra
 * @since 1.15
 */
public class Bundle
{

  /** Field description */
  private static final String SEPARATOR = System.getProperty("line.separator");

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param bundle
   */
  private Bundle(ResourceBundle bundle)
  {
    this.bundle = bundle;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Creates a new bundle instance
   *
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
   * @return formated message
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
   * @return formated message
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
   * @return formated message or null
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

  //~--- fields ---------------------------------------------------------------

  /** resource bundle */
  private final ResourceBundle bundle;
}
