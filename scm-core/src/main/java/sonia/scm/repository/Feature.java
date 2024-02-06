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

package sonia.scm.repository;

/**
 * Features which are supported by a {@link Repository}.
 *
 * @since 1.25
 */
public enum Feature
{

  /**
   * The default branch of the repository is a combined branch of all
   * repository branches.
   */
  COMBINED_DEFAULT_BRANCH,
  /**
   * The repository supports computation of incoming changes (either diff or list of changesets) of one branch
   * in respect to another target branch.
   */
  INCOMING_REVISION,
  /**
   * The repository supports computation of modifications between two revisions, not only for a singe revision.
   *
   * @since 2.23.0
   */
  MODIFICATIONS_BETWEEN_REVISIONS,
  /**
   * The repository supports pushing with a force flag.
   *
   * @since 2.47.0
   */
  FORCE_PUSH
}
