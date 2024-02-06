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
    
package sonia.scm.repository;


import com.google.inject.Inject;

import sonia.scm.plugin.Extension;

import java.io.File;

/**
 * Simple {@link HealthCheck} for mercurial repositories.
 *
 * @since 1.39
 */
@Extension
public final class HgHealthCheck extends DirectoryHealthCheck
{

  private static final HealthCheckFailure COULD_NOT_FIND_DOT_HG_DIRECTORY =
    new HealthCheckFailure("6bOdhOXpB1", "Could not find .hg directory",
      "The mercurial repository does not contain .hg directory.");

  private static final String DOT_HG = ".hg";


 
  @Inject
  public HgHealthCheck(RepositoryManager repositoryManager)
  {
    super(repositoryManager);
  }



  @Override
  protected HealthCheckResult check(Repository repository, File directory)
  {
    HealthCheckResult result = HealthCheckResult.healthy();
    File dotHgDirectory = new File(directory, DOT_HG);

    if (!dotHgDirectory.exists())
    {
      result = HealthCheckResult.unhealthy(COULD_NOT_FIND_DOT_HG_DIRECTORY);
    }

    return result;
  }


  /**
   * Returns {@code true} if the repository is from type mercurial.
   *
   *
   * @param repository repository for the health check
   *
   * @return {@code true} for a mercurial repository
   */
  @Override
  protected boolean isCheckResponsible(Repository repository)
  {
    return HgRepositoryHandler.TYPE_NAME.equalsIgnoreCase(repository.getType());
  }
}
