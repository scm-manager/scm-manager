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

package sonia.scm.repository.client.api;

import sonia.scm.repository.client.spi.MergeCommand;
import sonia.scm.repository.client.spi.MergeRequest;

import java.io.IOException;

/**
 * @since 2.4.0
 */
public final class MergeCommandBuilder {

  private final MergeCommand command;
  private final MergeRequest request = new MergeRequest();

  MergeCommandBuilder(MergeCommand command) {
    this.command = command;
  }

  public MergeCommandBuilder ffOnly() {
    request.setFfMode(MergeRequest.FastForwardMode.FF_ONLY);
    return this;
  }

  public MergeCommandBuilder noFf() {
    request.setFfMode(MergeRequest.FastForwardMode.NO_FF);
    return this;
  }

  public MergeCommandBuilder ffIfPossible() {
    request.setFfMode(MergeRequest.FastForwardMode.FF);
    return this;
  }

  public void merge(String branch) throws IOException {
    request.setBranch(branch);
    command.merge(request);
  }
}
