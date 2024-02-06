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
 * Enumeration of available hook features.
 *
 * @since 1.33
 */
public enum HookFeature
{

  /**
   * Hook message provider
   */
  MESSAGE_PROVIDER,

  /**
   * Hook changeset provider
   */
  CHANGESET_PROVIDER,

  /**
   * Hook branch provider
   *
   * @since 1.45
   */
  BRANCH_PROVIDER,

  /**
   * Hook tag provider
   *
   * @since 1.50
   */
  TAG_PROVIDER,

  /**
   * Provider to detect merges
   *
   * @since 2.4.0
   */
  MERGE_DETECTION_PROVIDER,

  /**
   * Provider to compute modifications
   *
   * @since 2.48.0
   */
  MODIFICATIONS_PROVIDER
}
