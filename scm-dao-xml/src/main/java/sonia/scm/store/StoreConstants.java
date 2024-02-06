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

package sonia.scm.store;

/**
 * Store constants for xml implementations.
 *
 */
public class StoreConstants
{

  private StoreConstants() {  }

  public static final String CONFIG_DIRECTORY_NAME = "config";

  /**
   * Name of the parent of data or blob directories.
   * @since 2.23.0
   */
  public static final String VARIABLE_DATA_DIRECTORY_NAME = "var";

  /**
   * Name of data directories.
   * @since 2.23.0
   */
  public static final String DATA_DIRECTORY_NAME = "data";

  /**
   * Name of blob directories.
   * @since 2.23.0
   */
  public static final String BLOG_DIRECTORY_NAME = "data";

  public static final String REPOSITORY_METADATA = "metadata";

  public static final String FILE_EXTENSION = ".xml";
}
