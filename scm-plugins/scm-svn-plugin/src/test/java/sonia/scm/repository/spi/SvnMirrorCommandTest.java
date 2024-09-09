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
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.GlobalProxyConfiguration;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.MirrorCommandResult;
import sonia.scm.util.SystemUtil;

import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.OK;

@RunWith(MockitoJUnitRunner.class)
public class SvnMirrorCommandTest extends AbstractSvnCommandTestBase {

  @Mock
  private X509TrustManager trustManager;

  @Mock
  private SvnMirrorAuthenticationFactory authenticationFactory;

  private SvnContext emptyContext;

  private final ScmConfiguration configuration = new ScmConfiguration();

  @Before
  public void bendContextToNewRepository() throws IOException, SVNException {
    emptyContext = createEmptyContext();
  }

  @Test
  public void shouldDoInitialMirror() {
    MirrorCommandResult result = callMirror(emptyContext, repositoryDirectory);

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
  public void shouldUseSourceUrlForAuthentication() throws SVNException, IOException {
    SvnMirrorCommand command = new SvnMirrorCommand(createEmptyContext(), authenticationFactory);
    MirrorCommandRequest request = createRequest(repositoryDirectory);

    command.mirror(request);

    verify(authenticationFactory).create(SVNURL.parseURIEncoded(request.getSourceUrl()), request);
  }

  private MirrorCommandResult callMirrorUpdate(SvnContext context, File source) {
    MirrorCommandRequest request = createRequest(source);
    return createMirrorCommand(context).update(request);
  }

  private MirrorCommandResult callMirror(SvnContext context, File source) {
    return createMirrorCommand(context).mirror(createRequest(source));
  }

  private MirrorCommandRequest createRequest(File source) {
    MirrorCommandRequest request = new MirrorCommandRequest();
    request.setSourceUrl("file://" + (SystemUtil.isWindows() ? "/" : "") + source.getAbsolutePath());
    return request;
  }

  private SvnMirrorCommand createMirrorCommand(SvnContext context) {
    return new SvnMirrorCommand(context, trustManager, new GlobalProxyConfiguration(configuration));
  }

  private SvnContext createEmptyContext() throws SVNException, IOException {
    File dir = tempFolder.newFolder();
    SVNRepositoryFactory.createLocalRepository(dir, true, true);
    return new SvnContext(RepositoryTestData.createHappyVerticalPeopleTransporter(), dir);
  }
}
