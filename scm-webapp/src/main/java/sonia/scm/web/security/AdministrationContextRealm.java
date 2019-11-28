package sonia.scm.web.security;

import com.google.common.collect.Sets;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;
import sonia.scm.security.Role;

@Extension
public class AdministrationContextRealm extends AuthorizingRealm {

  private static final Logger LOG = LoggerFactory.getLogger(AdministrationContextRealm.class);

  public AdministrationContextRealm() {
    setName(DefaultAdministrationContext.REALM);
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    AdministrationContextMarker marker = principals.oneByType(AdministrationContextMarker.class);
    if (marker == AdministrationContextMarker.MARKER) {
      LOG.info("assign admin permissions to admin context user {}", principals.getPrimaryPrincipal());
      SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo(Sets.newHashSet(Role.USER));
      authorizationInfo.setStringPermissions(Sets.newHashSet("*"));
      return authorizationInfo;
    }
    return null;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
    // we make no authentication we do only authorization
    return null;
  }
}
