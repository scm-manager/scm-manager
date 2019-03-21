package sonia.scm.web.security;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdministrationContextRealmTest {

  private AdministrationContextRealm realm = new AdministrationContextRealm();

  @Test
  void shouldAssignAdminPermissions() {
    SimplePrincipalCollection collection = new SimplePrincipalCollection();
    collection.add("scm-system", DefaultAdministrationContext.REALM);
    collection.add(AdministrationContextMarker.MARKER, DefaultAdministrationContext.REALM);

    AuthorizationInfo authorizationInfo = realm.doGetAuthorizationInfo(collection);

    assertThat(authorizationInfo.getStringPermissions()).containsOnly("*");
  }

  @Test
  void shouldReturnNull() {
    SimplePrincipalCollection collection = new SimplePrincipalCollection();
    collection.add("scm-system", DefaultAdministrationContext.REALM);

    AuthorizationInfo authorizationInfo = realm.doGetAuthorizationInfo(collection);

    assertThat(authorizationInfo).isNull();
  }

}
