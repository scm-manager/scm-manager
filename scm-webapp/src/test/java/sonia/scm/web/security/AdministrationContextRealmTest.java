/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

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
