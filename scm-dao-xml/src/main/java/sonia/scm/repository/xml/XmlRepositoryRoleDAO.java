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

package sonia.scm.repository.xml;

import com.google.inject.Inject;
import jakarta.inject.Singleton;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleDAO;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.xml.AbstractXmlDAO;

import java.util.List;

@Singleton
public class XmlRepositoryRoleDAO extends AbstractXmlDAO<RepositoryRole, XmlRepositoryRoleDatabase>
  implements RepositoryRoleDAO {

  public static final String STORE_NAME = "repositoryRoles";

  @Inject
  public XmlRepositoryRoleDAO(ConfigurationStoreFactory storeFactory) {
    super(storeFactory
      .withType(XmlRepositoryRoleDatabase.class)
      .withName(STORE_NAME)
      .build());
  }

  @Override
  protected RepositoryRole clone(RepositoryRole role)
  {
    return role.clone();
  }

  @Override
  protected XmlRepositoryRoleDatabase createNewDatabase()
  {
    return new XmlRepositoryRoleDatabase();
  }

  @Override
  public List<RepositoryRole> getAll() {
    return (List<RepositoryRole>) super.getAll();
  }
}
