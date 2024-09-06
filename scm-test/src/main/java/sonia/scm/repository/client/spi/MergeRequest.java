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

package sonia.scm.repository.client.spi;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * @since 2.4.0
 */
public final class MergeRequest {

  private String branch;
  private String message;
  private FastForwardMode ffMode = FastForwardMode.FF;

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

  public void setFfMode(FastForwardMode ffMode) {
    this.ffMode = ffMode;
  }

  String getBranch() {
    return branch;
  }

  String getMessage() {
    return message;
  }

  public FastForwardMode getFfMode() {
    return ffMode;
  }

  public enum FastForwardMode {
    FF_ONLY, FF, NO_FF
  }
}
