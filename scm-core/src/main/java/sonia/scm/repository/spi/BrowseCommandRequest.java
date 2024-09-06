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

package sonia.scm.repository.spi;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import sonia.scm.repository.BrowserResult;

import java.util.function.Consumer;

/**
 * @since 1.17
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public final class BrowseCommandRequest extends FileBaseCommandRequest {

  public static final int DEFAULT_REQUEST_LIMIT = 100;

  private static final long serialVersionUID = 7956624623516803183L;

  private boolean disableLastCommit = false;
  private boolean disableSubRepositoryDetection = false;
  private boolean recursive = false;
  private int limit = DEFAULT_REQUEST_LIMIT;

  // WARNING / TODO: This field creates a reverse channel from the implementation to the API. This will break
  // whenever the API runs in a different process than the SPI (for example to run explicit hosts for git repositories).
  @EqualsAndHashCode.Exclude
  private final transient Consumer<BrowserResult> updater;

  private int offset;
  private boolean collapse;

  public BrowseCommandRequest() {
    this(null);
  }

  public BrowseCommandRequest(Consumer<BrowserResult> updater) {
    this.updater = updater;
  }

  @Override
  public BrowseCommandRequest clone() {
    BrowseCommandRequest clone;

    try {
      clone = (BrowseCommandRequest) super.clone();
    } catch (CloneNotSupportedException e) {

      // this shouldn't happen, since we are Cloneable
      throw new InternalError("CatCommandRequest seems not to be cloneable");
    }

    return clone;
  }

  /**
   * Returns true if the last commit is disabled.
   *
   * @return true if the last commit is disabled
   * @since 1.26
   */
  public boolean isDisableLastCommit() {
    return disableLastCommit;
  }

  /**
   * True to disable the last commit.
   *
   * @param disableLastCommit true to disable the last commit
   * @since 1.26
   */
  public void setDisableLastCommit(boolean disableLastCommit) {
    this.disableLastCommit = disableLastCommit;
  }

  /**
   * Returns true if the detection of sub repositories is disabled.
   *
   * @return true if sub repository detection is disabled.
   * @since 1.26
   */
  public boolean isDisableSubRepositoryDetection() {
    return disableSubRepositoryDetection;
  }

  /**
   * Enable or Disable sub repository detection. Default is enabled.
   *
   * @param disableSubRepositoryDetection true to disable sub repository detection
   * @since 1.26
   */
  public void setDisableSubRepositoryDetection(
    boolean disableSubRepositoryDetection) {
    this.disableSubRepositoryDetection = disableSubRepositoryDetection;
  }

  /**
   * Returns true if recursive file object browsing is enabled.
   *
   * @return true recursive is enabled
   * @since 1.26
   */
  public boolean isRecursive() {
    return recursive;
  }

  /**
   * True to enable recursive file object browsing.
   *
   * @param recursive true to enable recursive browsing
   * @since 1.26
   */
  public void setRecursive(boolean recursive) {
    this.recursive = recursive;
  }

  /**
   * Returns the limit for the number of result files.
   *
   * @since 2.0.0
   */
  public int getLimit() {
    return limit;
  }

  /**
   * Limit the number of result files to <code>limit</code> entries.
   *
   * @param limit The maximal number of files this request shall return.
   * @since 2.0.0
   */
  public void setLimit(int limit) {
    this.limit = limit;
  }

  /**
   * The number of the entry, the result start with. All preceding entries will be omitted.
   *
   * @since 2.0.0
   */
  public int getOffset() {
    return offset;
  }

  /**
   * Proceed the list from the given number on (zero based).
   *
   * @param offset The number of the entry, the result should start with (zero based).
   *               All preceding entries will be omitted.
   * @since 2.0.0
   */
  public void setOffset(int offset) {
    this.offset = offset;
  }

  /**
   * Returns whether empty folders are collapsed until a folder has content and return the path to such folder as a
   * single item or not.
   *
   * @return {@code true} if empty folders are collapsed, otherwise {@code false}
   * @since 2.30.3
   */
  public boolean isCollapse() {
    return collapse;
  }

  /**
   * Collapse empty folders until a folder has content and return the path to such folder as a single item.
   *
   * @param collapse {@code true} if empty folders should be collapsed, otherwise {@code false}.
   * @since 2.30.3
   */
  public void setCollapse(boolean collapse) {
    this.collapse = collapse;
  }

  public void updateCache(BrowserResult update) {
    if (updater != null) {
      updater.accept(update);
    }
  }

}
