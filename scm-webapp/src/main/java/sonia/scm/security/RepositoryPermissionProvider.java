package sonia.scm.security;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleDAO;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.net.URL;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.unmodifiableCollection;

public class RepositoryPermissionProvider {

  private final SystemRepositoryPermissionProvider systemRepositoryPermissionProvider;
  private final RepositoryRoleDAO repositoryRoleDAO;

  @Inject
  public RepositoryPermissionProvider(SystemRepositoryPermissionProvider systemRepositoryPermissionProvider, RepositoryRoleDAO repositoryRoleDAO) {
    this.systemRepositoryPermissionProvider = systemRepositoryPermissionProvider;
    this.repositoryRoleDAO = repositoryRoleDAO;
  }

  public Collection<String> availableVerbs() {
    return systemRepositoryPermissionProvider.availableVerbs();
  }

  public Collection<RepositoryRole> availableRoles() {
    List<RepositoryRole> customRoles = repositoryRoleDAO.getAll();
    List<RepositoryRole> availableSystemRoles = systemRepositoryPermissionProvider.availableRoles();

    return new AbstractList<RepositoryRole>() {
      @Override
      public RepositoryRole get(int index) {
        return index < availableSystemRoles.size()? availableSystemRoles.get(index): customRoles.get(index - availableSystemRoles.size());
      }

      @Override
      public int size() {
        return availableSystemRoles.size() + customRoles.size();
      }
    };
  }
}
