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

package sonia.scm.repository.spi;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.TrackingRefUpdate;

/** A fetched ref update or an unchanged ref that still needs filter evaluation. */
final class GitMirrorRefUpdate {
  private final String localName;
  private final ObjectId oldObjectId;
  private final ObjectId newObjectId;
  private final RefUpdate.Result result;

  GitMirrorRefUpdate(TrackingRefUpdate update) {
    localName = update.getLocalName();
    oldObjectId = update.getOldObjectId();
    newObjectId = update.getNewObjectId();
    result = update.getResult();
  }

  GitMirrorRefUpdate(Ref ref) {
    localName = ref.getName();
    oldObjectId = ref.getObjectId();
    newObjectId = ref.getObjectId();
    result = RefUpdate.Result.NO_CHANGE;
  }

  String getLocalName() {
    return localName;
  }

  ObjectId getOldObjectId() {
    return oldObjectId;
  }

  ObjectId getNewObjectId() {
    return newObjectId;
  }

  RefUpdate.Result getResult() {
    return result;
  }

  boolean isUnchanged() {
    return oldObjectId.equals(newObjectId);
  }

  ReceiveCommand asReceiveCommand() {
    return new ReceiveCommand(oldObjectId, newObjectId, localName);
  }
}
