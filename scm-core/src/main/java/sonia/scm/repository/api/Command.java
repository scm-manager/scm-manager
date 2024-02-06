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

/**
 * Enumeration of available commands.
 *
 * @since 1.17
 */
public enum Command
{
  LOG, BROWSE, CAT, DIFF, BLAME,

  /**
   * @since 1.18
   */
  TAGS,

  /**
   * @since 1.18
   */
  BRANCHES,

  /**
   * @since 1.31
   */
  INCOMING, OUTGOING, PUSH, PULL,

  /**
   * @since 1.43
   */
  BUNDLE, UNBUNDLE,

  /**
   * @since 2.0
   */
  MODIFICATIONS, MERGE, DIFF_RESULT, BRANCH, MODIFY,

  /**
   * @since 2.10.0
   */
  LOOKUP,

  /**
   * @since 2.11.0
   */
  TAG,

  /**
   * @since 2.17.0
   */
  FULL_HEALTH_CHECK,

  /**
   * @since 2.19.0
   */
  MIRROR,

  /**
   * @since 2.26.0
   */
  FILE_LOCK,

  /**
   * @since 2.28.0
   */
  BRANCH_DETAILS,
  /**
   * @since 2.39.0
   */
  CHANGESETS
}
