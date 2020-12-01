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
