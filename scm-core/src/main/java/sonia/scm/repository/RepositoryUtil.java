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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Preconditions;
import sonia.scm.io.DirectoryFileFilter;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------


/**
 *
 * @author Sebastian Sdorra
 * @since 1.11
 */
public final class RepositoryUtil {

  private RepositoryUtil() {}

  public static List<File> searchRepositoryDirectories(File directory, String... names) {
    List<File> repositories = new ArrayList<>();

    searchRepositoryDirectories(repositories, directory, Arrays.asList(names));

    return repositories;
  }

  @SuppressWarnings("squid:S2083") // ignore, because the path is validated at {@link #getRepositoryId(File, File)}
  public static String getRepositoryId(AbstractRepositoryHandler handler, String directoryPath) throws IOException {
    return getRepositoryId(handler.getConfig().getRepositoryDirectory(), new File(directoryPath));
  }

  public static String getRepositoryId(AbstractRepositoryHandler handler, File directory) throws IOException {
    return getRepositoryId(handler.getConfig(), directory);
  }

  public static String getRepositoryId(SimpleRepositoryConfig config, File directory) throws IOException {
    return getRepositoryId(config.getRepositoryDirectory(), directory);
  }

  public static String getRepositoryId(File baseDirectory, File directory) throws IOException {
    String path = directory.getCanonicalPath();
    String basePath = baseDirectory.getCanonicalPath();

    Preconditions.checkArgument(
      path.startsWith(basePath),
      "repository path %s is not in the main repository path %s", path, basePath
    );

    String id = IOUtil.trimSeperatorChars(path.substring(basePath.length()));

    Preconditions.checkArgument(
      !id.contains("\\") && !id.contains("/"),
      "got illegal repository directory with separators in id: %s", path
    );

    return id;
  }

  private static void searchRepositoryDirectories(List<File> repositories, File directory, List<String> names) {
    boolean found = false;

    for (String name : names) {
      if (new File(directory, name).exists()) {
        found = true;

        break;
      }
    }

    if (found) {
      repositories.add(directory);
    } else {
      File[] directories = directory.listFiles(DirectoryFileFilter.instance);

      if (directories != null) {
        for (File d : directories) {
          searchRepositoryDirectories(repositories, d, names);
        }
      }
    }
  }
}
