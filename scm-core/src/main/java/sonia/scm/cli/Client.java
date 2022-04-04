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

package sonia.scm.cli;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import javax.annotation.Nullable;
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
