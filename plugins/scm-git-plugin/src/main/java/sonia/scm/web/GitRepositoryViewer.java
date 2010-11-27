/**
 * Copyright (c) 2010, Sebastian Sdorra
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



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.io.RegexResourceProcessor;
import sonia.scm.io.ResourceProcessor;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitRepositoryViewer
{

  /** Field description */
  public static final String MIMETYPE_HTML = "text/html";

  /** Field description */
  public static final String RESOURCE_GITINDEX = "/sonia/scm/git.index.html";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param response
   * @param repository
   * @param repositoryName
   *
   * @throws IOException
   * @throws NoHeadException
   * @throws ServletException
   */
  public void handleRequest(HttpServletResponse response,
                            Repository repository, String repositoryName)
          throws IOException, ServletException, NoHeadException
  {
    response.setContentType(MIMETYPE_HTML);

    ResourceProcessor processor = new RegexResourceProcessor();

    processor.addVariable("name", repositoryName);

    StringBuilder sb = new StringBuilder();

    if (!repository.getAllRefs().isEmpty())
    {
      Git git = new Git(repository);

      for (RevCommit commit : git.log().call())
      {
        appendCommit(sb, commit);
      }
    }

    processor.addVariable("commits", sb.toString());

    BufferedReader reader = null;
    PrintWriter writer = null;

    try
    {
      reader = new BufferedReader(
          new InputStreamReader(
              GitRepositoryViewer.class.getResourceAsStream(
                RESOURCE_GITINDEX)));
      writer = response.getWriter();
      processor.process(reader, writer);
    }
    finally
    {
      IOUtil.close(reader);
      IOUtil.close(writer);
    }
  }

  /**
   * Method description
   *
   *
   * @param sb
   * @param commit
   */
  private void appendCommit(StringBuilder sb, RevCommit commit)
  {
    sb.append("<tr><td class=\"small\">");

    long time = commit.getCommitTime();

    sb.append(Util.formatDate(new Date(time * 1000)));
    sb.append("</td><td class=\"small\">");

    PersonIdent person = commit.getCommitterIdent();

    if (person != null)
    {
      String name = person.getName();

      if (Util.isNotEmpty(name))
      {
        sb.append(name);
      }
    }

    sb.append("</td><td>").append(commit.getFullMessage());
    sb.append("</td></tr>");
  }
}
