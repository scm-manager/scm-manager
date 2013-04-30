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



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.net.URL;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * TODO add events
 *
 * @author Sebastian Sdorra
 * @since 1.31
 */
@Singleton
public class DefaultSecuritySystem implements SecuritySystem
{

  /** Field description */
  private static final String NAME = "security";

  /** Field description */
  private static final String PERMISSION_DESCRIPTOR =
    "META-INF/scm/permissions.xml";

  /**
   * the logger for DefaultSecuritySystem
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultSecuritySystem.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param storeFactory
   */
  @Inject
  public DefaultSecuritySystem(ConfigurationEntryStoreFactory storeFactory)
  {
    store = storeFactory.getStore(AssignedPermission.class, NAME);
    readAvailablePermissions();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param permission
   *
   * @return
   */
  @Override
  public StoredAssignedPermission addPermission(AssignedPermission permission)
  {
    assertIsAdmin();

    String id = store.put(permission);

    return new StoredAssignedPermission(id, permission);
  }

  /**
   * Method description
   *
   *
   * @param permission
   */
  @Override
  public void deletePermission(StoredAssignedPermission permission)
  {
    assertIsAdmin();
    deletePermission(permission.getId());
  }

  /**
   * Method description
   *
   *
   * @param id
   */
  @Override
  public void deletePermission(String id)
  {
    assertIsAdmin();
    store.remove(id);
  }

  /**
   * Method description
   *
   *
   * @param permission
   */
  @Override
  public void modifyPermission(StoredAssignedPermission permission)
  {
    assertIsAdmin();

    synchronized (store)
    {
      store.remove(permission.getId());
      store.put(permission.getId(), new AssignedPermission(permission));
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public List<StoredAssignedPermission> getAllPermissions()
  {
    return getPermissions(null);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public List<PermissionDescriptor> getAvailablePermissions()
  {
    assertIsAdmin();

    return availablePermissions;
  }

  /**
   * Method description
   *
   *
   * @param predicate
   *
   * @return
   */
  @Override
  public List<StoredAssignedPermission> getPermissions(
    Predicate<AssignedPermission> predicate)
  {
    Builder<StoredAssignedPermission> permissions = ImmutableList.builder();

    for (Entry<String, AssignedPermission> e : store.getAll().entrySet())
    {
      if ((predicate == null) || predicate.apply(e.getValue()))
      {
        permissions.add(new StoredAssignedPermission(e.getKey(), e.getValue()));
      }
    }

    return permissions.build();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public PrincipalCollection getSystemAccount()
  {

    // TODO
    throw new UnsupportedOperationException("Not supported yet.");
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  private void assertIsAdmin()
  {
    SecurityUtils.getSubject().checkRole(Role.ADMIN);
  }

  /**
   * Method description
   *
   *
   * @param context
   * @param descriptorUrl
   *
   * @return
   */
  private List<PermissionDescriptor> parsePermissionDescriptor(
    JAXBContext context, URL descriptorUrl)
  {
    List<PermissionDescriptor> descriptors = Collections.EMPTY_LIST;

    try
    {
      PermissionDescriptors descriptorWrapper =
        (PermissionDescriptors) context.createUnmarshaller().unmarshal(
          descriptorUrl);

      descriptors = descriptorWrapper.getPermissions();

      logger.debug("found {} permissions at {}", descriptors.size(),
        descriptorUrl);
      logger.trace("permissions from {}: {}", descriptorUrl, descriptors);
    }
    catch (JAXBException ex)
    {
      logger.error("could not parse permission descriptor", ex);
    }

    return descriptors;
  }

  /**
   * Method description
   *
   */
  private void readAvailablePermissions()
  {
    Builder<PermissionDescriptor> builder = ImmutableList.builder();

    try
    {
      JAXBContext context =
        JAXBContext.newInstance(PermissionDescriptors.class);

      Enumeration<URL> descirptorEnum =
        getClassLoader().getResources(PERMISSION_DESCRIPTOR);

      while (descirptorEnum.hasMoreElements())
      {
        URL descriptorUrl = descirptorEnum.nextElement();

        logger.debug("read permission descriptor from {}", descriptorUrl);

        builder.addAll(parsePermissionDescriptor(context, descriptorUrl));
      }
    }
    catch (IOException ex)
    {
      logger.error("could not read permission descriptors", ex);
    }
    catch (JAXBException ex)
    {
      logger.error(
        "could not create jaxb context to read permission descriptors", ex);
    }

    availablePermissions = builder.build();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private ClassLoader getClassLoader()
  {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    if (classLoader == null)
    {
      logger.warn("could not find context classloader, use default");

      classLoader = DefaultSecuritySystem.class.getClassLoader();
    }

    return classLoader;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/04/30
   * @author         Enter your name here...
   */
  @XmlRootElement(name = "permissions")
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class PermissionDescriptors
  {

    /**
     * Constructs ...
     *
     */
    public PermissionDescriptors() {}

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public List<PermissionDescriptor> getPermissions()
    {
      if (permissions == null)
      {
        permissions = Collections.EMPTY_LIST;
      }

      return permissions;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    @XmlElement(name = "permission")
    private List<PermissionDescriptor> permissions;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ConfigurationEntryStore<AssignedPermission> store;

  /** Field description */
  private List<PermissionDescriptor> availablePermissions;
}
