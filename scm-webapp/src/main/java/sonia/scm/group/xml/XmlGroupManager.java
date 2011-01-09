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



package sonia.scm.group.xml;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.HandlerEvent;
import sonia.scm.SCMContextProvider;
import sonia.scm.group.AbstractGroupManager;
import sonia.scm.group.Group;
import sonia.scm.group.GroupAllreadyExistExeption;
import sonia.scm.group.GroupException;
import sonia.scm.security.SecurityContext;
import sonia.scm.store.Store;
import sonia.scm.store.StoreFactory;
import sonia.scm.util.SecurityUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class XmlGroupManager extends AbstractGroupManager
{

  /** Field description */
  public static final String STORE_NAME = "groups";

  /** Field description */
  public static final String TYPE = "xml";

  /** the logger for XmlGroupManager */
  private static final Logger logger =
    LoggerFactory.getLogger(XmlGroupManager.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param securityContextProvider
   * @param storeFactory
   */
  @Inject
  public XmlGroupManager(Provider<SecurityContext> securityContextProvider,
                         StoreFactory storeFactory)
  {
    this.securityContextProvider = securityContextProvider;
    this.store = storeFactory.getStore(XmlGroupDatabase.class, STORE_NAME);
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
    if (logger.isInfoEnabled())
    {
      logger.info("create group {} of type {}", group.getName(),
                  group.getType());
    }

    SecurityUtil.assertIsAdmin(securityContextProvider);

    if (groupDB.contains(group.getName()))
    {
      throw new GroupAllreadyExistExeption();
    }

    String type = group.getType();

    if (Util.isEmpty(type))
    {
      group.setType(TYPE);
    }

    group.setCreationDate(System.currentTimeMillis());

    synchronized (XmlGroupManager.class)
    {
      groupDB.add(group.clone());
      storeDB();
    }

    fireEvent(group, HandlerEvent.CREATE);
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

    SecurityUtil.assertIsAdmin(securityContextProvider);

    String name = group.getName();

    if (groupDB.contains(name))
    {
      synchronized (XmlGroupManager.class)
      {
        groupDB.remove(name);
        storeDB();
      }

      fireEvent(group, HandlerEvent.DELETE);
    }
    else
    {
      throw new GroupException("user does not exists");
    }
  }

  /**
   * Method description
   *
   *
   * @param context
   */
  @Override
  public void init(SCMContextProvider context)
  {
    groupDB = store.get();

    if (groupDB == null)
    {
      groupDB = new XmlGroupDatabase();
      groupDB.setCreationTime(System.currentTimeMillis());
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
  public void modify(Group group) throws GroupException, IOException
  {
    if (logger.isInfoEnabled())
    {
      logger.info("modify group {} of type {}", group.getName(),
                  group.getType());
    }

    SecurityUtil.assertIsAdmin(securityContextProvider);

    String name = group.getName();

    if (groupDB.contains(name))
    {
      synchronized (XmlGroupManager.class)
      {
        groupDB.remove(name);
        groupDB.add(group.clone());
        storeDB();
      }

      fireEvent(group, HandlerEvent.MODIFY);
    }
    else
    {
      throw new GroupException("group does not exists");
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
    if (logger.isInfoEnabled())
    {
      logger.info("refresh group {} of type {}", group.getName(),
                  group.getType());
    }

    SecurityUtil.assertIsAdmin(securityContextProvider);

    Group fresh = groupDB.get(group.getName());

    if (fresh == null)
    {
      throw new GroupException("group does not exists");
    }

    fresh.copyProperties(group);
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
    Group group = groupDB.get(id);

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
    SecurityUtil.assertIsAdmin(securityContextProvider);

    LinkedList<Group> groups = new LinkedList<Group>();

    for (Group group : groupDB.values())
    {
      groups.add(group.clone());
    }

    return groups;
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
    LinkedList<Group> groups = new LinkedList<Group>();

    for (Group group : groupDB.values())
    {
      if (group.isMember(member))
      {
        groups.add(group.clone());
      }
    }

    return groups;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  private void storeDB()
  {
    groupDB.setLastModified(System.currentTimeMillis());
    store.set(groupDB);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private XmlGroupDatabase groupDB;

  /** Field description */
  private Provider<SecurityContext> securityContextProvider;

  /** Field description */
  private Store<XmlGroupDatabase> store;
}
