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

import com.github.sdorra.ssp.PermissionActionCheck;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.HandlerEventType;
import sonia.scm.SCMContextProvider;
import sonia.scm.TransformFilter;
import sonia.scm.search.SearchRequest;
import sonia.scm.search.SearchUtil;
import sonia.scm.util.CollectionAppender;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class DefaultGroupManager extends AbstractGroupManager
{

  /** the logger for XmlGroupManager */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultGroupManager.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param groupDAO
   */
  @Inject
  public DefaultGroupManager(GroupDAO groupDAO)
  {
    this.groupDAO = groupDAO;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {

    // do nothing
  }

  /**
   * Method description
   *
   *
   * @param group
   *
   * @throws GroupException
   * @throws IOException
   */
  @Override
  public void create(Group group) throws GroupException, IOException
  {
    String type = group.getType();

    if (Util.isEmpty(type))
    {
      group.setType(groupDAO.getType());
    }

    String name = group.getName();
    
    if (logger.isInfoEnabled())
    {
      logger.info("create group {} of type {}", name,
        group.getType());
    }

    GroupPermissions.create().check();
    
    if (groupDAO.contains(name))
    {
      throw new GroupAllreadyExistExeption(name.concat(" group already exists"));
    }

    removeDuplicateMembers(group);
    group.setCreationDate(System.currentTimeMillis());
    fireEvent(HandlerEventType.BEFORE_CREATE, group);
    groupDAO.add(group);
    fireEvent(HandlerEventType.CREATE, group);
  }

  /**
   * Method description
   *
   *
   * @param group
   *
   * @throws GroupException
   * @throws IOException
   */
  @Override
  public void delete(Group group) throws GroupException, IOException
  {
    if (logger.isInfoEnabled())
    {
      logger.info("delete group {} of type {}", group.getName(),
        group.getType());
    }

    String name = group.getName();
    GroupPermissions.delete().check(name);

    if (groupDAO.contains(name))
    {
      fireEvent(HandlerEventType.BEFORE_DELETE, group);
      groupDAO.delete(group);
      fireEvent(HandlerEventType.DELETE, group);
    }
    else
    {
      throw new GroupNotFoundException("user does not exists");
    }
  }

  /**
   * Method description
   *
   *
   * @param context
   */
  @Override
  public void init(SCMContextProvider context) {}

  /**
   * Method description
   *
   *
   * @param group
   *
   * @throws GroupException
   * @throws IOException
   */
  @Override
  public void modify(Group group) throws GroupException, IOException
  {
    if (logger.isInfoEnabled())
    {
      logger.info("modify group {} of type {}", group.getName(),
        group.getType());
    }

    String name = group.getName();
    GroupPermissions.modify().check(name);

    Group oldGroup = groupDAO.get(name);

    if (oldGroup != null)
    {
      removeDuplicateMembers(group);
      group.setLastModified(System.currentTimeMillis());
      fireEvent(HandlerEventType.BEFORE_MODIFY, group, oldGroup);
      groupDAO.modify(group);
      fireEvent(HandlerEventType.MODIFY, group, oldGroup);
    }
    else
    {
      throw new GroupNotFoundException("group does not exists");
    }
  }

  /**
   * Method description
   *
   *
   * @param group
   *
   * @throws GroupException
   * @throws IOException
   */
  @Override
  public void refresh(Group group) throws GroupException, IOException
  {
    String name = group.getName();
    if (logger.isInfoEnabled())
    {
      logger.info("refresh group {} of type {}", name, group.getType());
    }

    GroupPermissions.read(name).check();
    Group fresh = groupDAO.get(name);

    if (fresh == null)
    {
      throw new GroupNotFoundException("group does not exists");
    }

    fresh.copyProperties(group);
  }

  /**
   * Method description
   *
   *
   * @param searchRequest
   *
   * @return
   */
  @Override
  public Collection<Group> search(final SearchRequest searchRequest)
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("search group with query {}", searchRequest.getQuery());
    }

    final PermissionActionCheck<Group> check = GroupPermissions.read();
    return SearchUtil.search(searchRequest, groupDAO.getAll(),
      new TransformFilter<Group>()
    {
      @Override
      public Group accept(Group group)
      {
        Group result = null;

        if (check.isPermitted(group) && matches(searchRequest, group))
        {
          result = group.clone();
        }

        return result;
      }
    });
  }
  
  private boolean matches(SearchRequest searchRequest, Group group) {
    return SearchUtil.matchesOne(searchRequest, group.getName(), group.getDescription());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public Group get(String id)
  {
    GroupPermissions.read(id).check();
    
    Group group = groupDAO.get(id);

    if (group != null)
    {
      group = group.clone();
    }

    return group;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Group> getAll()
  {
    return getAll(null);
  }

  /**
   * Method description
   *
   *
   * @param comparator
   *
   * @return
   */
  @Override
  public Collection<Group> getAll(Comparator<Group> comparator)
  {
    List<Group> groups = new ArrayList<>();

    PermissionActionCheck<Group> check = GroupPermissions.read();
    for (Group group : groupDAO.getAll())
    {
      if (check.isPermitted(group)) {
        groups.add(group.clone());
      }
    }

    if (comparator != null)
    {
      Collections.sort(groups, comparator);
    }

    return groups;
  }

  /**
   * Method description
   *
   *
   *
   * @param comparator
   * @param start
   * @param limit
   *
   * @return
   */
  @Override
  public Collection<Group> getAll(Comparator<Group> comparator, int start,
    int limit)
  {
    final PermissionActionCheck<Group> check = GroupPermissions.read();

    return Util.createSubCollection(groupDAO.getAll(), comparator,
      new CollectionAppender<Group>()
    {
      @Override
      public void append(Collection<Group> collection, Group group)
      {
        if (check.isPermitted(group)) {
          collection.add(group.clone());
        }
      }
    }, start, limit);
  }

  /**
   * Method description
   *
   *
   * @param start
   * @param limit
   *
   * @return
   */
  @Override
  public Collection<Group> getAll(int start, int limit)
  {
    return getAll(null, start, limit);
  }

  /**
   * Method description
   *
   *
   * @param member
   *
   * @return
   */
  @Override
  public Collection<Group> getGroupsForMember(String member)
  {
    LinkedList<Group> groups = new LinkedList<>();

    for (Group group : groupDAO.getAll())
    {
      if (group.isMember(member))
      {
        groups.add(group.clone());
      }
    }

    return groups;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Long getLastModified()
  {
    return groupDAO.getLastModified();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Remove duplicate members from group.
   * Have a look at issue #439
   *
   *
   * @param group group
   */
  private void removeDuplicateMembers(Group group)
  {
    List<String> members =
      Lists.newArrayList(ImmutableSet.copyOf(group.getMembers()));

    group.setMembers(members);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private GroupDAO groupDAO;
}
