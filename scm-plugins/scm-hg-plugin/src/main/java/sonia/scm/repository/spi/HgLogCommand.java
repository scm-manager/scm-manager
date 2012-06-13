/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.HgContext;
import sonia.scm.repository.HgPythonScript;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.util.Util;
import sonia.scm.web.HgUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgLogCommand extends AbstractHgCommand implements LogCommand
{

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param context
   * @param repository
   * @param repositoryDirectory
   */
  public HgLogCommand(HgRepositoryHandler handler, HgContext context,
                      Repository repository, File repositoryDirectory)
  {
    super(handler, context, repository, repositoryDirectory);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public Changeset getChangeset(String id)
          throws IOException, RepositoryException
  {
    Map<String, String> env = Maps.newHashMap();

    env.put(ENV_REVISION, HgUtil.getRevision(id));
    env.put(ENV_PATH, Util.EMPTY_STRING);
    env.put(ENV_PAGE_START, "0");
    env.put(ENV_PAGE_LIMIT, "1");
    env.put(ENV_REVISION_START, Util.EMPTY_STRING);
    env.put(ENV_REVISION_END, Util.EMPTY_STRING);

    return getResultFromScript(Changeset.class, HgPythonScript.LOG, env);
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public ChangesetPagingResult getChangesets(LogCommandRequest request)
          throws IOException, RepositoryException
  {
    Map<String, String> env = Maps.newHashMap();

    env.put(ENV_REVISION, Util.EMPTY_STRING);
    env.put(ENV_PATH, Strings.nullToEmpty(request.getPath()));
    env.put(ENV_PAGE_START, String.valueOf(request.getPagingStart()));
    env.put(ENV_PAGE_LIMIT, String.valueOf(request.getPagingLimit()));
    env.put(ENV_REVISION_START,
            Strings.nullToEmpty(request.getStartChangeset()));
    env.put(ENV_REVISION_END, Strings.nullToEmpty(request.getEndChangeset()));

    return getResultFromScript(ChangesetPagingResult.class, HgPythonScript.LOG,
                               env);
  }
}
