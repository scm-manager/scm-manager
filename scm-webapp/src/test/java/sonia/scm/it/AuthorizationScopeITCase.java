/**
 * Copyright (c) 2014, Sebastian Sdorra
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
package sonia.scm.it;

import com.google.common.base.Strings;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.ScmState;
import static sonia.scm.it.IntegrationTestUtil.*;
import static sonia.scm.it.RepositoryITUtil.*;
import static sonia.scm.it.RepositoryITUtil.createRepository;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

/**
 * Integration test for authorization with scope.
 * 
 * @author Sebastian Sdorra
 */
public class AuthorizationScopeITCase {
  
  private Repository heartOfGold;
  private Repository puzzle42;
  
  /**
   * Create test repositories.
   */
  @Before
  public void createTestRepositories(){
    Client adminClient = createAdminClient();
    this.heartOfGold = createRepository(adminClient, RepositoryTestData.createHeartOfGold("git"));
    this.puzzle42 = createRepository(adminClient, RepositoryTestData.create42Puzzle("git"));
  }
  
  /**
   * Delete test repositories.
   */
  @After
  public void deleteTestRepositories(){
    Client adminClient = createAdminClient();
    deleteRepository(adminClient, heartOfGold.getId());
    deleteRepository(adminClient, puzzle42.getId());
  }
  
  /**
   * Read all available repositories without scope.
   */
  @Test
  public void testAuthenticateWithoutScope() {
    Assert.assertEquals(2, getRepositories(createAuthenticationToken()).size());
  }
  
  /**
   * Read all available repositories with a scope for only one of them.
   */
  @Test
  public void testAuthenticateWithScope() {
    String scope = "repository:read:".concat(heartOfGold.getId());
    Assert.assertEquals(1, getRepositories(createAuthenticationToken(scope)).size());
  }
  
  private List<Repository> getRepositories(String token) {
    Client client = createClient();
    WebResource wr =  client.resource(createResourceUrl("repositories"));
    return wr.header("Authorization", "Bearer ".concat(token)).get(new GenericType<List<Repository>>(){});
  }
  
  private String createAuthenticationToken() {
    return createAuthenticationToken("");
  }
  
  private String createAuthenticationToken(String scope) {
    Client client = createClient();
    String url = createResourceUrl("auth/access_token");
    
    WebResource wr =  client.resource(url);
    MultivaluedMap<String, String> formData = new MultivaluedMapImpl();

    formData.add("username", ADMIN_USERNAME);
    formData.add("password", ADMIN_PASSWORD);
    formData.add("grant_type", "password");
    if (!Strings.isNullOrEmpty(scope)) {
      formData.add("scope", scope);
    }

    ClientResponse response = wr.type("application/x-www-form-urlencoded").post(ClientResponse.class, formData);
    if (response.getStatus() >= 300 ){
      Assert.fail("authentication failed with status code " + response.getStatus());
    }
    
    return response.getEntity(ScmState.class).getToken();
  }
  
}
