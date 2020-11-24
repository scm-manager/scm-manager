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

import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.Repository;
import sonia.scm.repository.Tag;
import sonia.scm.repository.TagCreatedEvent;
import sonia.scm.repository.TagDeletedEvent;
import sonia.scm.repository.spi.TagCommand;

import java.io.IOException;

public class TagCommandBuilder {
  private final Repository repository;
  private final TagCommand command;
  private final ScmEventBus eventBus;

  public TagCommandBuilder(Repository repository, TagCommand command) {
    this.repository = repository;
    this.command = command;
    this.eventBus = ScmEventBus.getInstance();
  }

  TagCreateCommandBuilder create() {
    return new TagCreateCommandBuilder();
  }

  TagDeleteCommandBuilder delete() {
    return new TagDeleteCommandBuilder();
  }

  private class TagCreateCommandBuilder {
    private TagCreateRequest request = new TagCreateRequest();

    void setRevision(String revision) {
      request.setRevision(revision);
    }

    void setName(String name) {
      request.setName(name);
    }

    Tag execute() throws IOException {
      Tag tag = command.create(request);
      eventBus.post(new TagCreatedEvent(repository, request.getRevision(), request.getName()));
      return tag;
    }
  }

  private class TagDeleteCommandBuilder {
    private TagDeleteRequest request = new TagDeleteRequest();
    void execute() throws IOException {
      command.delete(request);
      eventBus.post(new TagDeletedEvent(repository, request.getName()));
    }
  }
}
