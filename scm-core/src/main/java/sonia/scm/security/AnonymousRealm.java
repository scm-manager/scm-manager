package sonia.scm.security;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.AuthenticatingRealm;

public class AnonymousRealm extends AuthenticatingRealm {

  /**
   * realm name
   */
  @VisibleForTesting
  static final String REALM = "AnonymousRealm";

  /**
   * dao realm helper
   */
  private final DAORealmHelper helper;

  @Inject
  public AnonymousRealm(DAORealmHelperFactory helperFactory) {
    this.helper = helperFactory.create(REALM);

    setAuthenticationTokenClass(AnonymousToken.class);
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
    return helper.authenticationInfoBuilder("_anonymous").build();
  }
}
