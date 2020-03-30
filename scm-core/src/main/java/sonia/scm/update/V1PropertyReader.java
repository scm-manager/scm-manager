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
    
package sonia.scm.update;

import java.util.Map;
import java.util.function.BiConsumer;

public interface V1PropertyReader {

  V1PropertyReader REPOSITORY_PROPERTY_READER = new RepositoryV1PropertyReader();
  V1PropertyReader USER_PROPERTY_READER = new UserV1PropertyReader();
  V1PropertyReader GROUP_PROPERTY_READER = new GroupV1PropertyReader();

  String getStoreName();

  Instance createInstance(Map<String, V1Properties> all);

  interface Instance {
    /**
     * Will call the given consumer for each id of the corresponding entity with its list of
     * properties converted from v1.
     * For example for repositories this will call the consumer with the id of each repository
     * that had properties attached in v1.
     */
    void forEachEntry(BiConsumer<String, V1Properties> propertiesForNameConsumer);

    /**
     * Filters for entities only having at least one property with a given key name.
     */
    Instance havingAnyOf(String... keys);

    /**
     * Filters for entities only having properties for all given key name.
     */
    Instance havingAllOf(String... keys);
  }
}
