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

import jakarta.inject.Inject;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreDecoratorFactory;

import java.util.Set;

public class AuditLogConfigurationStoreDecoratorFactory implements ConfigurationStoreDecoratorFactory {

  private final Set<Auditor> auditors;
  private final RepositoryDAO repositoryDAO;

  @Inject
  public AuditLogConfigurationStoreDecoratorFactory(Set<Auditor> auditor, RepositoryDAO repositoryDAO) {
    this.auditors = auditor;
    this.repositoryDAO = repositoryDAO;
  }

  @Override
  public <T> ConfigurationStore<T> createDecorator(ConfigurationStore<T> object, Context context) {
    return new AuditLogConfigurationStoreDecorator<>(auditors, repositoryDAO, object, context);
  }
}
