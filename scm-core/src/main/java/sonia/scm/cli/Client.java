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

package sonia.scm.cli;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;

@Value
@RequiredArgsConstructor
public class Client {
  String name;
  String version;
  @Nullable
  String commitHash;
  @Nullable
  Instant buildTime;
  @Nullable
  String os;
  @Nullable
  String arch;

  public Client(String name, String version) {
    this.name = name;
    this.version = version;
    this.commitHash = null;
    this.buildTime = null;
    this.os = null;
    this.arch = null;
  }

  public Optional<String> getCommitHash() {
    return Optional.ofNullable(commitHash);
  }

  public Optional<Instant> getBuildTime() {
    return Optional.ofNullable(buildTime);
  }

  public Optional<String> getOs() {
    return Optional.ofNullable(os);
  }

  public Optional<String> getArch() {
    return Optional.ofNullable(arch);
  }
}
