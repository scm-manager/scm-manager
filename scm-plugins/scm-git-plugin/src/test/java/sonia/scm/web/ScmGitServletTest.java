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

package sonia.scm.web;

import org.junit.Test;

import static org.eclipse.jgit.lfs.lib.Constants.CONTENT_TYPE_GIT_LFS_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by omilke on 11.05.2017.
 */
public class ScmGitServletTest {

  @Test
  public void isContentTypeMatches() throws Exception {

    assertThat(ScmGitServlet.isLfsContentHeaderField("application/vnd.git-lfs+json", CONTENT_TYPE_GIT_LFS_JSON), is(true));
    assertThat(ScmGitServlet.isLfsContentHeaderField("application/vnd.git-lfs+json;", CONTENT_TYPE_GIT_LFS_JSON), is(true));
    assertThat(ScmGitServlet.isLfsContentHeaderField("application/vnd.git-lfs+json; charset=utf-8", CONTENT_TYPE_GIT_LFS_JSON), is(true));

    assertThat(ScmGitServlet.isLfsContentHeaderField("application/vnd.git-lfs-json;", CONTENT_TYPE_GIT_LFS_JSON), is(false));
    assertThat(ScmGitServlet.isLfsContentHeaderField("", CONTENT_TYPE_GIT_LFS_JSON), is(false));
    assertThat(ScmGitServlet.isLfsContentHeaderField(null, CONTENT_TYPE_GIT_LFS_JSON), is(false));
  }

}
