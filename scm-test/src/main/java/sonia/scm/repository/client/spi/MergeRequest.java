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

package sonia.scm.repository.client.spi;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * @since 2.4.0
 */
public final class MergeRequest {

  private String branch;
  private String message;

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    final MergeRequest other = (MergeRequest) obj;

    return Objects.equal(branch, other.branch)
      && Objects.equal(message, other.message);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(branch, message);
  }

  public void reset() {
    this.branch = null;
    this.message = null;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
                  .add("branch", branch)
                  .add("message", message)
                  .toString();
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  String getBranch() {
    return branch;
  }

  String getMessage() {
    return message;
  }
}
