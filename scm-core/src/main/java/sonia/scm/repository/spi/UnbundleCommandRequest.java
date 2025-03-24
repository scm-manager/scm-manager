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


import com.google.common.base.Objects;
import com.google.common.io.ByteSource;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.RepositoryHookEvent;

import java.util.function.Consumer;

/**
 * Request object for the unbundle command.
 *
 * @author Sebastian Sdorra
 * @since 1.43
 */
public final class UnbundleCommandRequest {

  private Consumer<RepositoryHookEvent> postEventSink = event ->
    ScmEventBus.getInstance().post(new PostReceiveRepositoryHookEvent(event));

  private final ByteSource archive;

  public UnbundleCommandRequest(ByteSource archive) {
    this.archive = archive;
  }


  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final UnbundleCommandRequest other = (UnbundleCommandRequest) obj;

    return Objects.equal(archive, other.archive);
  }

 
  @Override
  public int hashCode() {
    return Objects.hashCode(archive);
  }


  public ByteSource getArchive() {
    return archive;
  }


  public Consumer<RepositoryHookEvent> getPostEventSink() {
    return postEventSink;
  }

  public void setPostEventSink(Consumer<RepositoryHookEvent> postEventSink) {
    this.postEventSink = postEventSink;
  }

}
