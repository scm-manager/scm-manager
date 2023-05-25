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

import sonia.scm.store.StoreParameters;
import sonia.scm.store.StoreType;

public interface StoreUpdateStepUtilFactory {

  default UtilForTypeBuilder forType(StoreType type) {
    return new UtilForTypeBuilder(this, type);
  }

  final class UtilForTypeBuilder {
    private final StoreUpdateStepUtilFactory factory;
    private final StoreType type;

    public UtilForTypeBuilder(StoreUpdateStepUtilFactory factory, StoreType type) {
      this.factory = factory;
      this.type = type;
    }

    public UtilForNameBuilder forName(String name) {
      return new UtilForNameBuilder(factory, type, name);
    }
  }

  final class UtilForNameBuilder {

    private final StoreUpdateStepUtilFactory factory;
    private final StoreType type;
    private final String name;
    private String repositoryId;
    private String namespace;

    public UtilForNameBuilder(StoreUpdateStepUtilFactory factory, StoreType type, String name) {
      this.factory = factory;
      this.type = type;
      this.name = name;
    }

    public UtilForNameBuilder forRepository(String repositoryId) {
      this.repositoryId = repositoryId;
      return this;
    }

    public UtilForNameBuilder forNamespace(String namespace) {
      this.namespace = namespace;
      return this;
    }

    public StoreUpdateStepUtil build() {
      return factory.build(
        type,
        new StoreParameters() {
          @Override
          public String getName() {
            return name;
          }

          @Override
          public String getRepositoryId() {
            return repositoryId;
          }

          @Override
          public String getNamespace() {
            return namespace;
          }
        }
      );
    }
  }

  StoreUpdateStepUtil build(StoreType type, StoreParameters parameters);

  interface StoreUpdateStepUtil {
    void renameStore(String newName);

    void deleteStore();
  }
}
