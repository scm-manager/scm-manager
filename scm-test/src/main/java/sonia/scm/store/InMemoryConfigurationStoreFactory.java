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



package sonia.scm.store;

//~--- non-JDK imports --------------------------------------------------------

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * In memory configuration store factory for testing purposes.
 *
 * @author Sebastian Sdorra
 */
public class InMemoryConfigurationStoreFactory implements ConfigurationStoreFactory {

  private static final Map<Key, InMemoryConfigurationStore> STORES = new HashMap<>();

  @Override
  public ConfigurationStore getStore(TypedStoreParameters storeParameters) {
    Key key = new Key(storeParameters.getType(), storeParameters.getName(), storeParameters.getRepository() == null? "-": storeParameters.getRepository().getId());
    if (STORES.containsKey(key)) {
      return STORES.get(key);
    } else {
      InMemoryConfigurationStore<Object> store = new InMemoryConfigurationStore<>();
      STORES.put(key, store);
      return store;
    }
  }

  private static class Key {
    private final Class type;
    private final String name;
    private final String id;

    public Key(Class type, String name, String id) {
      this.type = type;
      this.name = name;
      this.id = id;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Key key = (Key) o;
      return Objects.equals(type, key.type) &&
        Objects.equals(name, key.name) &&
        Objects.equals(id, key.id);
    }

    @Override
    public int hashCode() {
      return Objects.hash(type, name, id);
    }
  }
}
