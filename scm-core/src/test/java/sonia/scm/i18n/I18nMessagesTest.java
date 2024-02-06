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


import org.junit.Test;

import sonia.scm.repository.Changeset;

import static org.junit.Assert.*;

import java.util.Locale;
import java.util.MissingResourceException;


public class I18nMessagesTest
{

   @Test
  public void testI18n()
  {
    /*
      lookup-order for this test:
      - TM_en (es specified, but not ava)
      - TM_<execution-locale>
      - TM

      This means that, if there is no default locale specified,  this test accidentally passes on non-german machines, an fails on german machines, since the execution locale is de_DE, which is checked even before the fallback locale is considered.
     */

    Locale.setDefault(Locale.ENGLISH);

    TestMessages msg = I18nMessages.get(TestMessages.class);

    assertEquals("Normal Key", msg.normalKey);
    assertEquals("Key with Annotation", msg.keyWithAnnotation);
    assertNull(msg.someObject);
    assertNotNull(msg.bundle);
    assertEquals(Locale.ENGLISH, msg.locale);
  }

   @Test
  public void testI18nOtherLanguage()
  {
    TestMessages msg = I18nMessages.get(TestMessages.class, Locale.GERMANY);

    assertEquals("Normaler Schlüssel", msg.normalKey);
    assertEquals("Schlüssel mit Annotation", msg.keyWithAnnotation);
    assertNull(msg.someObject);
    assertNotNull(msg.bundle);
    assertEquals(Locale.GERMANY, msg.locale);
  }

   @Test(expected = MissingResourceException.class)
  public void testMissingBundle()
  {
    Changeset msg = I18nMessages.get(Changeset.class);
  }
}
