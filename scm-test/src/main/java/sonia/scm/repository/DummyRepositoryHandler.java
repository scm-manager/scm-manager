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

package sonia.scm.repository;


import com.google.common.collect.Sets;
import jakarta.xml.bind.annotation.XmlRootElement;
import sonia.scm.AlreadyExistsException;
import sonia.scm.store.ConfigurationStoreFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;


public class DummyRepositoryHandler
  extends AbstractSimpleRepositoryHandler<DummyRepositoryHandler.DummyRepositoryConfig> {

  public static final String TYPE_DISPLAYNAME = "Dummy";

  public static final String TYPE_NAME = "dummy";

  public static final RepositoryType TYPE = new RepositoryType(TYPE_NAME, TYPE_DISPLAYNAME, Sets.newHashSet());

  private final Set<String> existingRepoNames = new HashSet<>();

  public DummyRepositoryHandler(ConfigurationStoreFactory storeFactory, RepositoryLocationResolver repositoryLocationResolver) {
    super(storeFactory, repositoryLocationResolver, null);
  }

  @Override
  public RepositoryType getType() {
    return TYPE;
  }


  @Override
  protected void create(Repository repository, File directory) {
    String key = repository.getNamespace() + "/" + repository.getName();
    if (existingRepoNames.contains(key)) {
      throw new AlreadyExistsException(repository);
    } else {
      existingRepoNames.add(key);
    }
  }

  @Override
  protected DummyRepositoryConfig createInitialConfig() {
    return new DummyRepositoryConfig();
  }

  @Override
  protected Class<DummyRepositoryConfig> getConfigClass() {
    return DummyRepositoryConfig.class;
  }

  @XmlRootElement(name = "config")
  public static class DummyRepositoryConfig extends RepositoryConfig {
    @Override
    public String getId() {
      return TYPE_NAME;
    }
  }
}
