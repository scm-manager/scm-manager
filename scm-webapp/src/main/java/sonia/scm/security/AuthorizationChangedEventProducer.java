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

package sonia.scm.security;

import com.github.legman.Subscribe;
import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.ModificationHandlerEvent;
import sonia.scm.event.HandlerEvent;
import sonia.scm.event.ScmEventBus;
import sonia.scm.group.Group;
import sonia.scm.group.GroupEvent;
import sonia.scm.group.GroupModificationEvent;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceEvent;
import sonia.scm.repository.NamespaceModificationEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.RepositoryModificationEvent;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.user.User;
import sonia.scm.user.UserEvent;
import sonia.scm.user.UserModificationEvent;

import java.util.Collection;

/**
 * Receives all kinds of events, which affects authorization relevant data and fires an
 * {@link AuthorizationChangedEvent} if authorization data has changed.
 *
 * @since 1.52
 */
@Singleton
@EagerSingleton
public class AuthorizationChangedEventProducer {

 
  private static final Logger logger = LoggerFactory.getLogger(AuthorizationChangedEventProducer.class);

  public AuthorizationChangedEventProducer() {
  }

  /**
   * Invalidates the cache of a user which was modified. The cache entries for the user will be invalidated for the
   * following reasons:
   * <ul>
   * <li>Admin or Active flag was modified.</li>
   * <li>New user created, for the case of old cache values</li>
   * <li>User deleted</li>
   * </ul>
   *
   * @param event user event
   */
  @Subscribe
  public void onEvent(UserEvent event) {
    if (event.getEventType().isPost()) {
      if (isModificationEvent(event)) {
        handleUserModificationEvent((UserModificationEvent) event);
      } else {
        handleUserEvent(event);
      }
    }
  }

  private boolean isModificationEvent(HandlerEvent<?> event) {
    return event instanceof ModificationHandlerEvent;
  }

  private void handleUserEvent(UserEvent event) {
    String username = event.getItem().getName();
    logger.debug(
      "fire authorization changed event for user {}, because of user {} event", username, event.getEventType()
    );
    fireEventForUser(username);
  }

  private void handleUserModificationEvent(UserModificationEvent event) {
    String username = event.getItem().getId();
    User beforeModification = event.getItemBeforeModification();
    if (isAuthorizationDataModified(event.getItem(), beforeModification)) {
      logger.debug(
        "fire authorization changed event for user {}, because of a authorization relevant field has changed",
        username
      );
      fireEventForUser(username);
    } else {
      logger.debug(
        "authorization changed event for user {} is not fired, because no authorization relevant field has changed",
        username
      );
    }
  }

  private boolean isAuthorizationDataModified(User user, User beforeModification) {
    return user.isActive() != beforeModification.isActive();
  }

  private void fireEventForUser(String username) {
    sendEvent(AuthorizationChangedEvent.createForUser(username));
  }

  /**
   * Invalidates the whole cache, if a repository has changed. The cache get cleared for one of the following reasons:
   * <ul>
   * <li>New repository created</li>
   * <li>Repository was removed</li>
   * <li>Archived, Public readable or permission field of the repository was modified</li>
   * </ul>
   *
   * @param event repository event
   */
  @Subscribe(async = false)
  public void onEvent(RepositoryEvent event) {
    if (event.getEventType().isPost()) {
      if (isModificationEvent(event)) {
        handleRepositoryModificationEvent((RepositoryModificationEvent) event);
      } else {
        handleRepositoryEvent(event);
      }
    }
  }

  @Subscribe
  public void onEvent(NamespaceEvent event) {
    if (event.getEventType().isPost() && isModificationEvent(event)) {
      handleNamespaceModificationEvent((NamespaceModificationEvent) event);
    }
  }

  private void handleRepositoryModificationEvent(RepositoryModificationEvent event) {
    Repository repository = event.getItem();
    if (isAuthorizationDataModified(repository.getPermissions(), event.getItemBeforeModification().getPermissions())) {
      logger.debug(
        "fire authorization changed event, because the permissions of repository {} have changed", repository
      );
      fireEventForEveryUser();
    } else if (!event.getItem().getNamespace().equals(event.getItemBeforeModification().getNamespace())) {
      logger.debug(
        "fire authorization changed event, because the namespace of repository {} has changed", repository
      );
      fireEventForEveryUser();
    } else {
      logger.debug(
        "authorization changed event is not fired, because non relevant field of repository {} has changed", repository
      );
    }
  }

  private void handleNamespaceModificationEvent(NamespaceModificationEvent event) {
    Namespace namespace = event.getItem();
    if (isAuthorizationDataModified(namespace.getPermissions(), event.getItemBeforeModification().getPermissions())) {
      logger.debug(
        "fire authorization changed event, because a relevant field of namespace {} has changed", namespace.getNamespace()
      );
      fireEventForEveryUser();
    } else {
      logger.debug(
        "authorization changed event is not fired, because non relevant field of namespace {} has changed",
        namespace.getNamespace()
      );
    }
  }

  private boolean isAuthorizationDataModified
    (Collection<RepositoryPermission> newPermissions, Collection<RepositoryPermission> permissionsBeforeModification) {
    return !(newPermissions.containsAll(permissionsBeforeModification) && permissionsBeforeModification.containsAll(newPermissions));
  }

  private void fireEventForEveryUser() {
    sendEvent(AuthorizationChangedEvent.createForEveryUser());
  }

  private void handleRepositoryEvent(RepositoryEvent event) {
    logger.debug(
      "fire authorization changed event, because of received {} event for repository {}",
      event.getEventType(), event.getItem().getName()
    );
    fireEventForEveryUser();
  }

  /**
   * Invalidates the whole cache if a group permission has changed and invalidates the cached entries of a user, if a
   * user permission has changed.
   *
   * @param event permission event
   */
  @Subscribe
  public void onEvent(AssignedPermissionEvent event) {
    if (event.getEventType().isPost()) {
      AssignedPermission permission = event.getPermission();
      if (permission.isGroupPermission()) {
        handleGroupPermissionChange(permission);
      } else {
        handleUserPermissionChange(permission);
      }
    }
  }

  private void handleGroupPermissionChange(AssignedPermission permission) {
    logger.debug(
      "fire authorization changed event for group {}, because permission {} has changed",
      permission.getName(), permission.getPermission()
    );
    fireEventForEveryUser();
  }

  private void handleUserPermissionChange(AssignedPermission permission) {
    logger.debug(
      "fire authorization changed event for user {}, because permission {} has changed",
      permission.getName(), permission.getPermission()
    );
    fireEventForUser(permission.getName());
  }

  /**
   * Invalidates the whole cache, if a group has changed. The cache get cleared for one of the following reasons:
   * <ul>
   * <li>New group created</li>
   * <li>Group was removed</li>
   * <li>Group members was modified</li>
   * </ul>
   *
   * @param event group event
   */
  @Subscribe
  public void onEvent(GroupEvent event) {
    if (event.getEventType().isPost()) {
      if (isModificationEvent(event)) {
        handleGroupModificationEvent((GroupModificationEvent) event);
      } else {
        handleGroupEvent(event);
      }
    }
  }

  private void handleGroupModificationEvent(GroupModificationEvent event) {
    Group group = event.getItem();
    if (isAuthorizationDataModified(group, event.getItemBeforeModification())) {
      logger.debug("fire authorization changed event, because group {} has changed", group.getId());
      fireEventForEveryUser();
    } else {
      logger.debug(
        "authorization changed event is not fired, because non relevant field of group {} has changed",
        group.getId()
      );
    }
  }

  private boolean isAuthorizationDataModified(Group group, Group beforeModification) {
    return !group.getMembers().equals(beforeModification.getMembers());
  }

  private void handleGroupEvent(GroupEvent event) {
    logger.debug(
      "fire authorization changed event, because of received group event {} for group {}",
      event.getEventType(),
      event.getItem().getId()
    );
    fireEventForEveryUser();
  }

  @VisibleForTesting
  protected void sendEvent(AuthorizationChangedEvent event) {
    ScmEventBus.getInstance().post(event);
  }
}
