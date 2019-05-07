package sonia.scm.repository.xml;

import com.google.inject.Inject;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleDAO;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.xml.AbstractXmlDAO;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class XmlRepositoryRoleDAO extends AbstractXmlDAO<RepositoryRole, XmlRepositoryRoleDatabase>
  implements RepositoryRoleDAO {

  public static final String STORE_NAME = "repositoryRoles";

  @Inject
  public XmlRepositoryRoleDAO(ConfigurationStoreFactory storeFactory) {
    super(storeFactory
      .withType(XmlRepositoryRoleDatabase.class)
      .withName(STORE_NAME)
      .build());
  }

  @Override
  protected RepositoryRole clone(RepositoryRole role)
  {
    return role.clone();
  }

  @Override
  protected XmlRepositoryRoleDatabase createNewDatabase()
  {
    return new XmlRepositoryRoleDatabase();
  }

  @Override
  public List<RepositoryRole> getAll() {
    return (List<RepositoryRole>) super.getAll();
  }
}
