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


import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.servlet.http.HttpServletRequest;
import sonia.scm.util.ClassLoaders;

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * The I18nMessages class instantiates a class and initializes all {@link String}
 * fields with values from a resource bundle. The resource bundle must have the
 * same name as the class. Each field which should be initialized from the
 * bundle, must match a key in the resource bundle or is annotated with a
 * {@link I18n} annotation which holds the key. I18nMessages injects also the
 * locale and the bundle if it founds a field with the corresponding type.
 *
 * @since 1.37
 */
public final class I18nMessages
{

  private static final Cache<CacheKey, Object> cache =
    CacheBuilder.newBuilder().build();


  private I18nMessages() {}


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
   */
  public static <T> T get(Class<T> msgClass, HttpServletRequest request)
  {
    return get(msgClass, request.getLocale());
  }

  /**
   * Returns an instance of the given message class with all message fields
   * initialized.
   *
   *
   * @param msgClass message class
   * @param locale locale
   * @param <T> type of the message class
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


  private static class CacheKey
  {
    private final Locale locale;

    private final Class msgClass;

    public CacheKey(Locale locale, Class msgClass)
    {
      this.locale = locale;
      this.msgClass = msgClass;
    }

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

    @Override
    public int hashCode()
    {
      return Objects.hashCode(locale, msgClass);
    }

  }
}
