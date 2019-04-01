package sonia.scm.group;

import sonia.scm.GenericDisplayManager;
import sonia.scm.search.SearchRequest;
import sonia.scm.search.SearchUtil;

import javax.inject.Inject;

public class DefaultGroupDisplayManager extends GenericDisplayManager<Group, DisplayGroup> implements GroupDisplayManager {

  @Inject
  public DefaultGroupDisplayManager(GroupDAO groupDAO) {
    super(groupDAO, DisplayGroup::from);
  }

  @Override
  protected void checkPermission() {
    GroupPermissions.autocomplete().check();
  }

  @Override
  protected boolean matches(SearchRequest searchRequest, Group group) {
    return SearchUtil.matchesOne(searchRequest, group.getName(), group.getDescription());
  }
}
