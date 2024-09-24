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

package sonia.scm.update.repository;

import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.SCMContextProvider;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.xml.XmlRepositoryDAO;
import sonia.scm.user.User;
import sonia.scm.user.xml.XmlUserDAO;
import sonia.scm.version.Version;

import static sonia.scm.version.Version.parse;

@Extension
public class PublicFlagUpdateStep implements UpdateStep {

  private static final Logger LOG = LoggerFactory.getLogger(PublicFlagUpdateStep.class);

  private static final String V1_REPOSITORY_BACKUP_FILENAME = "repositories.xml.v1.backup";

  private final SCMContextProvider contextProvider;
  private final XmlUserDAO userDAO;
  private final XmlRepositoryDAO repositoryDAO;

  @Inject
  public PublicFlagUpdateStep(SCMContextProvider contextProvider, XmlUserDAO userDAO, XmlRepositoryDAO repositoryDAO) {
    this.contextProvider = contextProvider;
    this.userDAO = userDAO;
    this.repositoryDAO = repositoryDAO;
  }

  @Override
  public void doUpdate() throws JAXBException {
    LOG.info("Migrating public flags of repositories as RepositoryRolePermission 'READ' for user '_anonymous'");
    V1RepositoryHelper.readV1Database(contextProvider, V1_REPOSITORY_BACKUP_FILENAME).ifPresent(
      v1RepositoryDatabase -> {
        createNewAnonymousUserIfNotExists();
        deleteOldAnonymousUserIfAvailable();
        addRepositoryReadPermissionForAnonymousUser(v1RepositoryDatabase);
      }
    );
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.3");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.repository.xml";
  }

  private void addRepositoryReadPermissionForAnonymousUser(V1RepositoryHelper.V1RepositoryDatabase v1RepositoryDatabase) {
    User v2AnonymousUser = userDAO.get(SCMContext.USER_ANONYMOUS);
    v1RepositoryDatabase.repositoryList.repositories
      .stream()
      .filter(V1Repository::isPublic)
      .forEach(v1Repository -> {
        Repository v2Repository = repositoryDAO.get(v1Repository.getId());
        if (v2Repository != null) {
          LOG.info("Add RepositoryRole 'READ' to _anonymous user for repository: {}", v2Repository);
          v2Repository.addPermission(new RepositoryPermission(v2AnonymousUser.getId(), "READ", false));
          repositoryDAO.modify(v2Repository);
        } else {
          LOG.info("Repository no longer found for id {}; could not set permission for former anonymous mode", v1Repository.getId());
        }
      });
  }

  private void createNewAnonymousUserIfNotExists() {
    if (!userExists(SCMContext.USER_ANONYMOUS)) {
      LOG.info("Create new _anonymous user");
      userDAO.add(SCMContext.ANONYMOUS);
    }
  }

  private void deleteOldAnonymousUserIfAvailable() {
    String oldAnonymous = "anonymous";
    if (userExists(oldAnonymous)) {
      User anonymousUser = userDAO.get(oldAnonymous);
      LOG.info("Delete obsolete anonymous user");
      userDAO.delete(anonymousUser);
    }
  }

  private boolean userExists(String username) {
    return userDAO
      .getAll()
      .stream()
      .anyMatch(user -> user.getName().equals(username));
  }
}
