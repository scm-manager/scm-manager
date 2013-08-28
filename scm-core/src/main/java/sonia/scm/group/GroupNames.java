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



package sonia.scm.group;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * This class represents all associated groups for a user.
 *
 * @author Sebastian Sdorra
 * @since 1.21
 */
public final class GroupNames implements Serializable, Iterable<String>
{

  /**
   * Group for all authenticated users
   * @since 1.31
   */
  public static final String AUTHENTICATED = "_authenticated";

  /** Field description */
  private static final long serialVersionUID = 8615685985213897947L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public GroupNames()
  {
    this.collection = Collections.EMPTY_LIST;
  }

  /**
   * Constructs ...
   *
   *
   * @param collection
   */
  public GroupNames(Collection<String> collection)
  {
    this.collection = Collections.unmodifiableCollection(collection);
  }

  /**
   * Constructs ...
   *
   *
   * @param groupName
   * @param groupNames
   */
  public GroupNames(String groupName, String... groupNames)
  {
    this.collection = Lists.asList(groupName, groupNames);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param groupName
   *
   * @return
   */
  public boolean contains(String groupName)
  {
    return collection.contains(groupName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    final GroupNames other = (GroupNames) obj;

    return Objects.equal(collection, other.collection);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(collection);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Iterator<String> iterator()
  {
    return collection.iterator();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<String> getCollection()
  {
    return collection;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Collection<String> collection;
}
