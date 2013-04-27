/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.store;

/**
 * The ConfigurationEntryStoreFactory can be used to create new or get existing
 * {@link ConfigurationEntryStore}s. <b>Note:</b> the default implementation 
 * uses the same location as the {@link StoreFactory}, so be sure that the 
 * store names are unique for all {@link ConfigurationEntryStore}s and 
 * {@link Store}s.
 * 
 * @author Sebastian Sdorra
 * @since 1.31
 * 
 * @apiviz.landmark
 * @apiviz.uses sonia.scm.store.ConfigurationEntryStore
 */
public interface ConfigurationEntryStoreFactory
{

  /**
   * Get an existing {@link ConfigurationEntryStore} or create a new one.
   *
   *
   * @param type type of the store objects
   * @param name name of the store
   * @param <T> type of the store objects
   *
   * @return {@link ConfigurationEntryStore} with given name and type
   */
  public <T> ConfigurationEntryStore<T> getStore(Class<T> type, String name);
}
