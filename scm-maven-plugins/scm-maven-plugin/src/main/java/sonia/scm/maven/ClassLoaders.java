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



package sonia.scm.maven;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public final class ClassLoaders
{

  /**
   * Constructs ...
   *
   */
  private ClassLoaders() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param project
   *
   * @return
   *
   * @throws DependencyResolutionRequiredException
   * @throws MalformedURLException
   */
  @SuppressWarnings("unchecked")
  public static ClassLoader createRuntimeClassLoader(MavenProject project)
    throws DependencyResolutionRequiredException, MalformedURLException
  {
    Set<URL> urls = new HashSet<>();

    append(urls, project.getRuntimeClasspathElements());
    append(urls, project.getCompileClasspathElements());

    return URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]),
      Thread.currentThread().getContextClassLoader());
  }

  /**
   * Method description
   *
   *
   * @param urls
   * @param elements
   *
   * @throws MalformedURLException
   */
  private static void append(Set<URL> urls, List<String> elements)
    throws MalformedURLException
  {
    for (String element : elements)
    {
      urls.add(new File(element).toURI().toURL());
    }
  }
}
