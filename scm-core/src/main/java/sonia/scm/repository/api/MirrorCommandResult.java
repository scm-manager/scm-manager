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

import com.google.common.annotations.Beta;

import java.time.Duration;
import java.util.List;

@Beta
public final class MirrorCommandResult {

  private final ResultType result;
  private final List<String> log;
  private final Duration duration;
  private final LfsUpdateResult lfsUpdateResult;

  public MirrorCommandResult(ResultType result, List<String> log, Duration duration) {
    this(result, log, duration, null);
  }

  public MirrorCommandResult(ResultType result, List<String> log, Duration duration, LfsUpdateResult lfsUpdateResult) {
    this.result = result;
    this.log = log;
    this.duration = duration;
    this.lfsUpdateResult = lfsUpdateResult;
  }

  public ResultType getResult() {
    return result;
  }

  public List<String> getLog() {
    return log;
  }

  public Duration getDuration() {
    return duration;
  }

  public LfsUpdateResult getLfsUpdateResult() {
    return lfsUpdateResult;
  }

  public enum ResultType {
    OK,
    REJECTED_UPDATES,
    FAILED
  }

  public static class LfsUpdateResult {
    private int overallCount = 0;
    private int failureCount = 0;

    public void increaseOverallCount() {
      overallCount++;
    }

    public void increaseFailureCount() {
      failureCount++;
    }

    public int getOverallCount() {
      return overallCount;
    }

    public int getFailureCount() {
      return failureCount;
    }

    public boolean hasFailures() {
      return failureCount > 0;
    }
  }
}
