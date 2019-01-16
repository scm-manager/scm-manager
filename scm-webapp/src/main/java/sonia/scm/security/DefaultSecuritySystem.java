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



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.github.legman.Subscribe;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.HandlerEventType;
import sonia.scm.event.ScmEventBus;
import sonia.scm.group.GroupEvent;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.user.UserEvent;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;

//~--- JDK imports ------------------------------------------------------------

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
  @SuppressWarnings("unchecked")
  public DefaultSecuritySystem(ConfigurationEntryStoreFactory storeFactory, PluginLoader pluginLoader)
  {
    store = storeFactory
      .withType(AssignedPermission.class)
      .withName(NAME)
      .build();
    this.availablePermissions = readAvailablePermissions(pluginLoader);
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
  public void addPermission(AssignedPermission permission)
  {
    assertIsAdmin();
    validatePermission(permission);

    String id = store.put(permission);

    StoredAssignedPermission sap = new StoredAssignedPermission(id, permission);

    //J-
    ScmEventBus.getInstance().post(
      new AssignedPermissionEvent(HandlerEventType.CREATE, permission)
    );
    //J+
  }

  /**
   * Method description
   *
   *
   * @param permission
   */
  @Override
  public void deletePermission(AssignedPermission permission)
  {
    assertIsAdmin();
    boolean deleted = deletePermissions(sap -> Objects.equal(sap.getName(), permission.getName())
      && Objects.equal(sap.isGroupPermission(), permission.isGroupPermission())
      && Objects.equal(sap.getPermission(), permission.getPermission()));
    if (deleted) {
      ScmEventBus.getInstance().post(
        new AssignedPermissionEvent(HandlerEventType.DELETE, permission)
      );
    }
  }

  /**
   * Method description
   *
   *
   * @param event
   */
  @Subscribe
  public void handleEvent(final UserEvent event)
  {
    if (event.getEventType() == HandlerEventType.DELETE)
    {
      deletePermissions(p -> !p.isGroupPermission()
        && event.getItem().getName().equals(p.getName()));
    }
  }

  /**
   * Method description
   *
   *
   * @param event
   */
  @Subscribe
  public void handleEvent(final GroupEvent event)
  {
    if (event.getEventType() == HandlerEventType.DELETE)
    {
      deletePermissions(p -> p.isGroupPermission()
        && event.getItem().getName().equals(p.getName()));
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
  public Collection<PermissionDescriptor> getAvailablePermissions()
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
  public Collection<AssignedPermission> getPermissions(Predicate<AssignedPermission> predicate)
  {
    Builder<AssignedPermission> permissions = ImmutableSet.builder();

    for (Entry<String, AssignedPermission> e : store.getAll().entrySet())
    {
      if ((predicate == null) || predicate.test(e.getValue()))
      {
        permissions.add(new StoredAssignedPermission(e.getKey(), e.getValue()));
      }
    }

    return permissions.build();
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
   * @param predicate
   */
  private boolean deletePermissions(Predicate<AssignedPermission> predicate)
  {
    boolean found = false;
    for (Entry<String, AssignedPermission> e : store.getAll().entrySet()) {
      if ((predicate == null) || predicate.test(e.getValue())) {
        store.remove(e.getKey());
        found = true;
      }
    }
    return found;
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
  @SuppressWarnings("unchecked")
  private static List<PermissionDescriptor> parsePermissionDescriptor(
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
   * @param pluginLoader
   */
  private static ImmutableSet<PermissionDescriptor> readAvailablePermissions(PluginLoader pluginLoader)
  {
    ImmutableSet.Builder<PermissionDescriptor> builder = ImmutableSet.builder();

    try
    {
      JAXBContext context =
        JAXBContext.newInstance(PermissionDescriptors.class);

      // Querying permissions from uberClassLoader returns also the permissions from plugin
      Enumeration<URL> descirptorEnum =
        pluginLoader.getUberClassLoader().getResources(PERMISSION_DESCRIPTOR);

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

    return builder.build();
  }

  /**
   * Method description
   *
   *
   * @param perm
   */
  private void validatePermission(AssignedPermission perm)
  {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(perm.getName()),
      "name is required");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(perm.getPermission()),
      "permission is required");
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Descriptor for permissions.
   */
  @XmlRootElement(name = "permissions")
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class PermissionDescriptors
  {

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    @SuppressWarnings("unchecked")
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
  private final ImmutableSet<PermissionDescriptor> availablePermissions;
}
