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
    
package sonia.scm.store;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Predicate;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 * A ConfigurationEntryStore can be used to store multiple entries of structured 
 * configuration data. <b>Note:</b> the default implementation use JAXB to 
 * marshall the items.
 *
 * @author Sebastian Sdorra
 *
 * @param <V> store value type
 * @since 1.31
 */
public interface ConfigurationEntryStore<V> extends DataStore<V> {

  /**
   * Return all values matching the given {@link Predicate}.
   *
   *
   * @param predicate predicate to match values
   *
   * @return filtered collection of values
   */
  public Collection<V> getMatchingValues(Predicate<V> predicate);
}
