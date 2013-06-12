/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.store;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 * The blob store can be used store unstructured data in form of a {@link Blob}.
 *
 * @author Sebastian Sdorra
 * @since 1.23
 *
 * @apiviz.uses sonia.scm.store.Blob
 */
public interface BlobStore extends StoreBase<Blob>
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
   * @throws EntryAllreadyExistsStoreException if a blob with given id already
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

  //~--- get methods ----------------------------------------------------------

  /**
   * Return all blob object which are stored in this BlobStore.
   *
   *
   * @return a list of all blob object
   */
  public List<Blob> getAll();
}
