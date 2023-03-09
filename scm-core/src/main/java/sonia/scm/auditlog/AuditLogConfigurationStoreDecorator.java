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

package sonia.scm.auditlog;

import com.google.common.base.Strings;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.StoreDecoratorFactory;

import java.util.Set;

public class AuditLogConfigurationStoreDecorator<T> implements ConfigurationStore<T> {

  private final Set<Auditor> auditors;
  private final RepositoryDAO repositoryDAO;
  private final ConfigurationStore<T> delegate;
  private final StoreDecoratorFactory.Context context;

  public AuditLogConfigurationStoreDecorator(Set<Auditor> auditors, RepositoryDAO repositoryDAO, ConfigurationStore<T> delegate, StoreDecoratorFactory.Context context) {
    this.auditors = auditors;
    this.repositoryDAO = repositoryDAO;
    this.delegate = delegate;
    this.context = context;
  }

  public T get() {
    return delegate.get();
  }

  public void set(T object) {
      String repositoryId = context.getStoreParameters().getRepositoryId();
      if (!Strings.isNullOrEmpty(repositoryId)) {
        String name = repositoryDAO.get(repositoryId).getNamespaceAndName().toString();
        auditors.forEach(s -> s.createEntry(new EntryCreationContext<>(object, get(), name, Set.of("repository"))));
      } else {
        auditors.forEach(s -> s.createEntry(new EntryCreationContext<>(object, get(), "", Set.of(context.getStoreParameters().getName()))));
      }
      delegate.set(object);
  }
}
