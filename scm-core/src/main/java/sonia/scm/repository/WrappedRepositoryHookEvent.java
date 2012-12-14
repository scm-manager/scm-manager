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


package sonia.scm.repository;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 * Base class for {@link RepositoryHookEvent} wrappers.
 *
 * @author Sebastian Sdorra
 * @since 1.23
 */
public class WrappedRepositoryHookEvent
{

  /**
   * Constructs a new WrappedRepositoryHookEvent.
   *
   *
   * @param wrappedEvent event to wrap
   */
  protected WrappedRepositoryHookEvent(RepositoryHookEvent wrappedEvent)
  {
    this.wrappedEvent = wrappedEvent;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Returns a wrapped instance of the {@link RepositoryHookEvent}-
   *
   *
   * @param event event to wrap
   *
   * @return wrapper
   */
  public static WrappedRepositoryHookEvent wrap(RepositoryHookEvent event)
  {
    WrappedRepositoryHookEvent wrappedEvent = null;

    switch (event.getType())
    {
      case POST_RECEIVE :
        wrappedEvent = new PostReceiveRepositoryHookEvent(event);

        break;

      case PRE_RECEIVE :
        wrappedEvent = new PreReceiveRepositoryHookEvent(event);

        break;

      default :
        throw new IllegalArgumentException("unsupported hook event type");
    }

    return wrappedEvent;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns a collection of changesets which are added with this repository
   * event.
   *
   *
   * @return a collection of added changesets
   */
  public Collection<Changeset> getChangesets()
  {
    return wrappedEvent.getChangesets();
  }

  /**
   * Returns the repository which was modified.
   *
   *
   * @return modified repository
   */
  public Repository getRepository()
  {
    return wrappedEvent.getRepository();
  }

  //~--- fields ---------------------------------------------------------------

  /** wrapped event */
  private RepositoryHookEvent wrappedEvent;
}
