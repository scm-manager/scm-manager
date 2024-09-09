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

package sonia.scm.repository.hooks;

import jakarta.inject.Singleton;

import java.util.UUID;

@Singleton
public class HookEnvironment {

  private final ThreadLocal<Boolean> threadEnvironment = new ThreadLocal<>();
  private final String challenge = UUID.randomUUID().toString();

  public String getChallenge() {
    return challenge;
  }

  public boolean isAcceptAble(String challenge) {
    return this.challenge.equals(challenge);
  }

  void setPending(boolean pending) {
    threadEnvironment.set(pending);
  }

  void clearPendingState() {
    threadEnvironment.remove();
  }

  public boolean isPending() {
    Boolean threadState = threadEnvironment.get();
    if (threadState != null) {
      return threadState;
    }
    return false;
  }

}
