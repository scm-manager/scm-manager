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
