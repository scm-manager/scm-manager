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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Sets;

import sonia.scm.ConfigChangedListener;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Set;

/**
 * Base class for {@link ListenableStore}. The AbstractListenableStore provides
 * methods for event and listener handling.
 *
 * @author Sebastian Sdorra
 * @since 1.16
 *
 * @param <T> type of store objects
 */
public abstract class AbstractListenableStore<T> implements ListenableStore<T>
{

  /**
   * Read the stored object.
   *
   *
   * @return stored object
   */
  protected abstract T readObject();

  /**
   * Write object to the store.
   *
   *
   * @param object object to write
   */
  protected abstract void writeObject(T object);

  /**
   * Add a listener to the store.
   *
   *
   * @param listener listener for store
   */
  @Override
  public void addListener(ConfigChangedListener<T> listener)
  {
    listeners.add(listener);
  }

  /**
   * Add a collection of listeners to the store.
   *
   *
   * @param listeners listeners for store
   */
  @Override
  public void addListeners(Collection<ConfigChangedListener<T>> listeners)
  {
    listeners.addAll(listeners);
  }

  /**
   * Remove a listener from the store
   *
   *
   * @param listener listener to remove
   */
  @Override
  public void removeListener(ConfigChangedListener<T> listener)
  {
    listeners.remove(listener);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public T get()
  {
    if (storeObject == null)
    {
      storeObject = readObject();
    }

    return storeObject;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * {@inheritDoc}
   *
   *
   * @param obejct
   */
  @Override
  public void set(T obejct)
  {
    writeObject(obejct);
    this.storeObject = obejct;
    fireEvent(obejct);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Fire a store event.
   *
   *
   * @param object changed object
   */
  protected void fireEvent(T object)
  {
    for (ConfigChangedListener<T> listener : listeners)
    {
      listener.configChanged(object);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** listeners */
  protected Set<ConfigChangedListener<T>> listeners = Sets.newHashSet();

  /** stored object */
  protected T storeObject;
}
