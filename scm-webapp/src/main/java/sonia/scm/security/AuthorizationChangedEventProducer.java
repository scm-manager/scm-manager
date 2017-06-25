/**
 * Copyright (c) 2014, Sebastian Sdorra
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
package sonia.scm.security;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.ModificationHandlerEvent;
import sonia.scm.event.HandlerEvent;
import sonia.scm.event.ScmEventBus;
import sonia.scm.group.Group;
import sonia.scm.group.GroupEvent;
import sonia.scm.group.GroupModificationEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.RepositoryModificationEvent;
import sonia.scm.user.User;
import sonia.scm.user.UserEvent;
import sonia.scm.user.UserModificationEvent;

/**
 * Receives all kinds of events, which affects authorization relevant data and fires an 
 * {@link AuthorizationChangedEvent} if authorization data has changed.
 * 
 * @author Sebastian Sdorra
 * @since 1.52
 */
@EagerSingleton
public class AuthorizationChangedEventProducer {
  
  /**
   * the logger for AuthorizationChangedEventProducer
   */
  private static final Logger logger = LoggerFactory.getLogger(AuthorizationChangedEventProducer.class);

  /**
   * Constructs a new instance.
   */
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
    return user.isAdmin() != beforeModification.isAdmin() || user.isActive() != beforeModification.isActive();
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
  @Subscribe
  public void onEvent(RepositoryEvent event) {
    if (event.getEventType().isPost()) {
      if (isModificationEvent(event)) {
        handleRepositoryModificationEvent((RepositoryModificationEvent) event);
      } else {
        handleRepositoryEvent(event);
      }
    }
  }
  
  private void handleRepositoryModificationEvent(RepositoryModificationEvent event) {
    Repository repository = event.getItem();
    if (isAuthorizationDataModified(repository, event.getItemBeforeModification())) {
      logger.debug(
        "fire authorization changed event, because a relevant field of repository {} has changed", repository.getName()
      );
      fireEventForEveryUser();
    } else {
      logger.debug(
        "authorization changed event is not fired, because non relevant field of repository {} has changed",
        repository.getName()
      );
    }
  }

  private boolean isAuthorizationDataModified(Repository repository, Repository beforeModification) {
    return repository.isArchived() != beforeModification.isArchived()
      || repository.isPublicReadable() != beforeModification.isPublicReadable()
      || ! repository.getPermissions().equals(beforeModification.getPermissions());
  }
  
  private void fireEventForEveryUser() {
    sendEvent(AuthorizationChangedEvent.createForEveryUser());
  }
  
  private void handleRepositoryEvent(RepositoryEvent event){
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
  public void onEvent(StoredAssignedPermissionEvent event) {
    if (event.getEventType().isPost()) {
      StoredAssignedPermission permission = event.getPermission();
      if (permission.isGroupPermission()) {
        handleGroupPermissionChange(permission);
      } else {
        handleUserPermissionChange(permission);
      }
    }
  }
  
  private void handleGroupPermissionChange(StoredAssignedPermission permission) {
    logger.debug(
      "fire authorization changed event, because global group permission {} has changed", 
      permission.getId()
    );
    fireEventForEveryUser();
  }
  
  private void handleUserPermissionChange(StoredAssignedPermission permission) {
    logger.debug(
        "fire authorization changed event for user {}, because permission {} has changed", 
        permission.getName(), permission.getId()
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
  
  private void handleGroupEvent(GroupEvent event){
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
