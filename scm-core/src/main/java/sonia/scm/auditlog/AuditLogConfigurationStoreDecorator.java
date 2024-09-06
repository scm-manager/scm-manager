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

package sonia.scm.auditlog;

import com.google.common.base.Strings;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.StoreDecoratorFactory;

import java.util.Optional;
import java.util.Set;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Collections.emptySet;

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

  @Override
  public T get() {
    return delegate.get();
  }

  @Override
  public void set(T object) {
    if (shouldAudit(object)) {
      auditors.forEach(s -> s.createEntry(createEntryCreationContext(object)));
    }
    delegate.set(object);
  }

  @Override
  public void delete() {
    if (shouldAudit(get())) {
      auditors.forEach(s -> s.createEntry(createEntryDeletionContext()));
    }
    delegate.delete();
  }

  private EntryCreationContext<T> createEntryCreationContext(T object) {
    return createContext(context.getStoreParameters().getRepositoryId(), get(), object);
  }

  private EntryCreationContext<T> createEntryDeletionContext() {
    return createContext(context.getStoreParameters().getRepositoryId(), get(), null);
  }

  private EntryCreationContext<T> createContext(String repositoryId, T oldObject, T newObject) {
    if (!Strings.isNullOrEmpty(repositoryId)) {
      String name = repositoryDAO.get(repositoryId).getNamespaceAndName().toString();
      return new EntryCreationContext<>(
        newObject,
        oldObject,
        name,
        getRepositoryLabels(firstNonNull(newObject, oldObject))
      );
    } else {
      return new EntryCreationContext<>(
        newObject,
        oldObject,
        shouldUseStoreNameAsLabel(firstNonNull(newObject, oldObject)) ? Set.of(context.getStoreParameters().getName()) : emptySet()
      );
    }
  }

  private boolean shouldAudit(T object) {
    return getAnnotation(object)
      .map(AuditEntry::ignore)
      .map(b -> !b)
      .orElse(true);
  }

  private Set<String> getRepositoryLabels(T object) {
    Set<String> labels = new java.util.HashSet<>();
    labels.add("repository");
    if (shouldUseStoreNameAsLabel(object)) {
      labels.add(context.getStoreParameters().getName());
    }
    return labels;
  }

  private boolean shouldUseStoreNameAsLabel(T object) {
    return getAnnotation(object).map(annotation -> annotation.labels().length == 0).orElse(true);
  }

  private Optional<AuditEntry> getAnnotation(T object) {
    return Optional.ofNullable(object.getClass().getAnnotation(AuditEntry.class));
  }
}
