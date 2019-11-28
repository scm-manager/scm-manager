package sonia.scm.user;

import sonia.scm.GenericDisplayManager;
import sonia.scm.search.SearchRequest;
import sonia.scm.search.SearchUtil;

import javax.inject.Inject;

public class DefaultUserDisplayManager extends GenericDisplayManager<User, DisplayUser> implements UserDisplayManager {

  @Inject
  public DefaultUserDisplayManager(UserDAO userDAO) {
    super(userDAO, DisplayUser::from);
  }

  @Override
  protected void checkPermission() {
    UserPermissions.autocomplete().check();
  }

  @Override
  protected boolean matches(SearchRequest searchRequest, User user) {
    return SearchUtil.matchesOne(searchRequest, user.getName(), user.getDisplayName(), user.getMail());
  }
}
