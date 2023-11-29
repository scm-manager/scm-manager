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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.servlet.http.HttpServletRequest;
import sonia.scm.util.ClassLoaders;

import java.lang.reflect.Field;
import java.util.Locale;

//~--- JDK imports ------------------------------------------------------------

/**
 * The I18nMessages class instantiates a class and initializes all {@link String}
 * fields with values from a resource bundle. The resource bundle must have the
 * same name as the class. Each field which should be initialized from the
 * bundle, must match a key in the resource bundle or is annotated with a
 * {@link I18n} annotation which holds the key. I18nMessages injects also the
 * locale and the bundle if it founds a field with the corresponding type.
 *
 * @author Sebastian Sdorra
 * @since 1.37
 */
public final class I18nMessages
{

  /** Field description */
  private static final Cache<CacheKey, Object> cache =
    CacheBuilder.newBuilder().build();

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private I18nMessages() {}

  //~--- get methods ----------------------------------------------------------

  /**
   * Same as {@link #get(java.lang.Class, java.util.Locale)}, with locale
   * {@link Locale#ENGLISH}.
   *
   * @param msgClass message class
   * @param <T> type of message class
   *
   * @return instance of message class
   */
  public static <T> T get(Class<T> msgClass)
  {
    return get(msgClass, Locale.ENGLISH);
  }

  /**
   * Same as {@link #get(java.lang.Class, java.util.Locale)}, with locale
   * from servlet request ({@link HttpServletRequest#getLocale()}).
   *
   *
   * @param msgClass message class
   * @param request servlet request
   * @param <T> type of message class
   *
   * @return
   */
  public static <T> T get(Class<T> msgClass, HttpServletRequest request)
  {
    return get(msgClass, request.getLocale());
  }

  /**
   * Returns a instance of the given message class with all message fields
   * initialized.
   *
   *
   * @param msgClass message class
   * @param locale locale
   * @param <T> type of the message class
   *
   * @return instance of message class
   */
  @SuppressWarnings("unchecked")
  public synchronized static <T> T get(Class<T> msgClass, Locale locale)
  {
    CacheKey ck = new CacheKey(locale, msgClass);
    T instance = (T) cache.getIfPresent(ck);

    if (instance == null)
    {
      instance = createInstance(msgClass, locale);
      cache.put(ck, instance);
    }

    return instance;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param msgClass
   * @param locale
   * @param <T>
   *
   * @return
   */
  private static <T> T createInstance(Class<T> msgClass, Locale locale)
  {
    Bundle bundle = Bundle.getBundle(msgClass.getName(), locale,
                      ClassLoaders.getContextClassLoader(msgClass));
    T instance = null;

    try
    {
      instance = msgClass.newInstance();
      initializeInstance(bundle, locale, msgClass, instance);
    }
    catch (Exception ex)
    {
      throw new I18nException("could not instantiate/initialize class", ex);
    }

    return instance;
  }

  /**
   * Method description
   *
   *
   * @param bundle
   * @param locale
   * @param msgClass
   * @param instance
   *
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  private static void initializeInstance(Bundle bundle, Locale locale,
    Class msgClass, Object instance)
    throws IllegalArgumentException, IllegalAccessException
  {
    for (Field field : msgClass.getDeclaredFields())
    {
      if (field.getType().isAssignableFrom(String.class))
      {
        String key = field.getName();
        I18n i18n = field.getAnnotation(I18n.class);

        if (i18n != null)
        {
          key = i18n.value();
        }

        String value = bundle.getString(key);

        if (value != null)
        {
          field.setAccessible(true);
          field.set(instance, value);
        }
      }
      else if (field.getType().isAssignableFrom(Bundle.class))
      {
        field.setAccessible(true);
        field.set(instance, bundle);
      }
      else if (field.getType().isAssignableFrom(Locale.class))
      {

        field.setAccessible(true);
        field.set(instance, locale);
      }
    }
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/03/15
   * @author         Enter your name here...
   */
  private static class CacheKey
  {

    /**
     * Constructs ...
     *
     *
     * @param locale
     * @param msgClass
     */
    public CacheKey(Locale locale, Class msgClass)
    {
      this.locale = locale;
      this.msgClass = msgClass;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param obj
     *
     * @return
     */
    @Override
    public boolean equals(Object obj)
    {
      if (obj == null)
      {
        return false;
      }

      if (getClass() != obj.getClass())
      {
        return false;
      }

      final CacheKey other = (CacheKey) obj;

      return Objects.equal(locale, other.locale)
        && Objects.equal(msgClass, other.msgClass);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int hashCode()
    {
      return Objects.hashCode(locale, msgClass);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final Locale locale;

    /** Field description */
    private final Class msgClass;
  }
}
