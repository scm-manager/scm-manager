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

import java.util.HashMap;
import java.util.Map;

/**
 * In memory configuration store factory for testing purposes.
 *
 * @author Sebastian Sdorra
 *
 * @deprecated Use the {@link InMemoryByteConfigurationStoreFactory} to verify JaxB serialization, too.
 */
@Deprecated(since = "2.44.0")
public class InMemoryConfigurationStoreFactory implements ConfigurationStoreFactory {

  private final Map<String, InMemoryConfigurationStore> stores = new HashMap<>();

  public static InMemoryConfigurationStoreFactory create() {
    return new InMemoryConfigurationStoreFactory();
  }

  @Override
  public ConfigurationStore getStore(TypedStoreParameters storeParameters) {
    String name = storeParameters.getName();
    String id = storeParameters.getRepositoryId();
    return get(name, id);
  }

  public ConfigurationStore get(String name, String id) {
    return stores.computeIfAbsent(buildKey(name, id), x -> new InMemoryConfigurationStore());
  }

  private String buildKey(String name, String id) {
    return id == null? name: name + "-" + id;
  }
}
