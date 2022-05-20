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

package sonia.scm.cli;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class CombinedResourceBundle extends ResourceBundle {

  private static final Map<String, Object> TRANSLATIONS = new HashMap<>();

  public CombinedResourceBundle(ResourceBundle... bundles) {
    Arrays.stream(bundles).forEach(resourceBundle -> {
      Enumeration<String> keys = resourceBundle.getKeys();
      for (String key : IterableEnumeration.make(keys)) {
        TRANSLATIONS.put(key, resourceBundle.getObject(key));
      }
    });
  }

  @Override
  protected Object handleGetObject(@Nonnull String key) {
    return TRANSLATIONS.get(key);
  }

  @Override
  protected Set<String> handleKeySet() {
    return TRANSLATIONS.keySet();
  }

  @Override
  public Enumeration<String> getKeys() {
    return Collections.enumeration(TRANSLATIONS.keySet());
  }

  static class IterableEnumeration<T> implements Iterable<T> {
    private final Enumeration<T> en;
    public IterableEnumeration(Enumeration<T> en) {
      this.en = en;
    }
    public Iterator<T> iterator() {
      return new Iterator<T>() {
        public boolean hasNext() {
          return en.hasMoreElements();
        }
        public T next() {
          return en.nextElement();
        }
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }
    public static <T> Iterable<T> make(Enumeration<T> en) {
      return new IterableEnumeration<T>(en);
    }
  }
}
