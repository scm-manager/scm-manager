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
