/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.repository.spi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.SVNAuthentication;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.MirrorCommandResult;
import sonia.scm.repository.api.SimpleUsernamePasswordCredential;

import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.OK;

@RunWith(MockitoJUnitRunner.class)
public class SvnMirrorCommandTest extends AbstractSvnCommandTestBase {

  @Mock
  private X509TrustManager trustManager;

  private SvnContext emptyContext;

  @Before
  public void bendContextToNewRepository() throws IOException, SVNException {
    emptyContext = createEmptyContext();
  }

  @Test
  public void shouldDoInitialMirror() {
    MirrorCommandResult result = callMirror(emptyContext, repositoryDirectory, c -> {
    });

    assertThat(result.getResult()).isEqualTo(OK);
  }

  @Test
  public void shouldDoMirrorUpdate() throws SVNException {
    // Initialize destination repo before update
    SVNAdminClient svnAdminClient = new SVNAdminClient(new BasicAuthenticationManager(new SVNAuthentication[]{}), SVNWCUtil.createDefaultOptions(false));
    svnAdminClient.doInitialize(SVNURL.fromFile(repositoryDirectory), emptyContext.createUrl());

    MirrorCommandResult result = callMirrorUpdate(emptyContext, repositoryDirectory);

    assertThat(result.getResult()).isEqualTo(OK);
    assertThat(result.getLog()).contains("Updated from revision 0 to revision 5");
  }

  @Test
  public void shouldDoMirrorUpdateOnNotInitializedRepo() {
    MirrorCommandResult result = callMirrorUpdate(emptyContext, repositoryDirectory);

    assertThat(result.getResult()).isEqualTo(OK);
    assertThat(result.getLog()).contains("Updated from revision 0 to revision 5");
  }

  @Test
  public void shouldUseCredentials() {
    MirrorCommandResult result = callMirror(emptyContext, repositoryDirectory, createCredential("svnadmin", "secret"));

    assertThat(result.getResult()).isEqualTo(OK);
  }

  private MirrorCommandResult callMirrorUpdate(SvnContext context, File source) {
    MirrorCommandRequest request = createRequest(source);
    return createMirrorCommand(context).update(request);
  }

  private MirrorCommandResult callMirror(SvnContext context, File source, Consumer<MirrorCommandRequest> consumer) {
    MirrorCommandRequest request = createRequest(source);
    consumer.accept(request);
    return createMirrorCommand(context).mirror(request);
  }

  private MirrorCommandRequest createRequest(File source) {
    MirrorCommandRequest request = new MirrorCommandRequest();
    request.setSourceUrl("file://" + source.getAbsolutePath());
    return request;
  }

  private SvnMirrorCommand createMirrorCommand(SvnContext context) {
    return new SvnMirrorCommand(context, trustManager);
  }

  private Consumer<MirrorCommandRequest> createCredential(String username, String password) {
    return request -> request.setCredentials(singletonList(new SimpleUsernamePasswordCredential(username, password.toCharArray())));
  }

  private SvnContext createEmptyContext() throws SVNException, IOException {
    File dir = tempFolder.newFolder();
    SVNRepositoryFactory.createLocalRepository(dir, true, true);
    return new SvnContext(RepositoryTestData.createHappyVerticalPeopleTransporter(), dir);
  }
}
