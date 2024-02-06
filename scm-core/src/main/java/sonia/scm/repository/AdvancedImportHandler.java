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
 * Searches and import existing repositories. The {@link AdvancedImportHandler}
 * gives more control over the result of the import as the
 * {@link ImportHandler}.
 *
 * @since 1.43
 * @deprecated
 */
@Deprecated
public interface AdvancedImportHandler extends ImportHandler
{

  /**
   * Import existing and non managed repositories. Returns result which
   * contains names of the successfully imported directories and the names of
   * the failed directories
   *
   *
   * @param manager The global {@link RepositoryManager}
   *
   * @return result which contains names of the successfully imported
   *   directories and the names of the failed directories.
   */
  public ImportResult importRepositoriesFromDirectory(
    RepositoryManager manager);
}
