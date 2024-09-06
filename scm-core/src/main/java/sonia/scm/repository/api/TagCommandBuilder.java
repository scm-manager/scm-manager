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

package sonia.scm.repository.api;

import sonia.scm.repository.Tag;
import sonia.scm.repository.spi.TagCommand;

import java.io.IOException;

/**
 * @since 2.11.0
 */
public final class TagCommandBuilder {
  private final TagCommand command;

  public TagCommandBuilder(TagCommand command) {
    this.command = command;
  }

  /**
   * Initialize a command to tag a revision.
   *
   * Set parameters and call {@link TagCreateCommandBuilder#execute()}.
   *
   * @since 2.11.0
   */
  public TagCreateCommandBuilder create() {
    return new TagCreateCommandBuilder();
  }

  /**
   * Initialize a command to delete a tag.
   *
   * Set parameters and call {@link TagDeleteCommandBuilder#execute()}.
   *
   * @since 2.11.0
   */
  public TagDeleteCommandBuilder delete() {
    return new TagDeleteCommandBuilder();
  }

  public final class TagCreateCommandBuilder {
    private final TagCreateRequest request = new TagCreateRequest();

    /**
     * @param revision The revision identifier for which to create the tag
     */
    public TagCreateCommandBuilder setRevision(String revision) {
      request.setRevision(revision);
      return this;
    }

    /**
     * @param name The name of the tag
     */
    public TagCreateCommandBuilder setName(String name) {
      request.setName(name);
      return this;
    }

    public Tag execute() throws IOException {
      return command.create(request);
    }
  }

  public final class TagDeleteCommandBuilder {
    private final TagDeleteRequest request = new TagDeleteRequest();

    /**
     * @param name The name of the tag that should be deleted
     */
    public TagDeleteCommandBuilder setName(String name) {
      request.setName(name);
      return this;
    }

    public void execute() throws IOException {
      command.delete(request);
    }
  }
}
