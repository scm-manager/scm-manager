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

import java.util.List;

/**
 * The blob store can be used store unstructured data in form of a {@link Blob}.
 *
 * @since 1.23
 *
 * @apiviz.uses sonia.scm.store.Blob
 */
public interface BlobStore extends MultiEntryStore<Blob>
{

  /**
   * Create a new blob object with automatically generated id.
   *
   *
   * @return new blob
   */
  public Blob create();

  /**
   * Create a new blob object with the given id.
   *
   *
   * @param id id of the new blob
   *
   * @return new blob
   *
   * @throws sonia.scm.store.EntryAlreadyExistsStoreException if a blob with given id already
   *   exists
   */
  public Blob create(String id);

  /**
   * Remove the given blob object-
   *
   *
   * @param blob blob object to remove
   */
  public void remove(Blob blob);


  /**
   * Return all blob object which are stored in this BlobStore.
   *
   *
   * @return a list of all blob object
   */
  public List<Blob> getAll();
}
