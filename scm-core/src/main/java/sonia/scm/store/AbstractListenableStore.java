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
 *
 * @author Sebastian Sdorra
 * @since 1.16
 *
 * @param <T>
 */
public abstract class AbstractListenableStore<T> implements ListenableStore<T>
{

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract T readObject();

  /**
   * Method description
   *
   *
   * @param object
   */
  protected abstract void writeObject(T object);

  /**
   * Method description
   *
   *
   * @param listener
   */
  @Override
  public void addListener(ConfigChangedListener<T> listener)
  {
    listeners.add(listener);
  }

  /**
   * Method description
   *
   *
   * @param listeners
   */
  @Override
  public void addListeners(Collection<ConfigChangedListener<T>> listeners)
  {
    listeners.addAll(listeners);
  }

  /**
   * Method description
   *
   *
   * @param listener
   */
  @Override
  public void removeListener(ConfigChangedListener<T> listener)
  {
    listeners.remove(listener);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
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
   * Method description
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
   * Method description
   *
   *
   * @param object
   */
  protected void fireEvent(T object)
  {
    for (ConfigChangedListener<T> listener : listeners)
    {
      listener.configChanged(object);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected Set<ConfigChangedListener<T>> listeners = Sets.newHashSet();

  /** Field description */
  protected T storeObject;
}
