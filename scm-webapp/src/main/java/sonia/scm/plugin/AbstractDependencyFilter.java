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



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Throwables;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractDependencyFilter implements DependencyFilter
{

  /**
   * the logger for AbstractDependencyFilter
   */
  private static final Logger logger =
    LoggerFactory.getLogger(AbstractDependencyFilter.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  protected abstract Set<String> loadExcludeSet() throws IOException;

  /**
   * Method description
   *
   *
   * @param node
   * @param parents
   *
   * @return
   */
  @Override
  public boolean accept(DependencyNode node, List<DependencyNode> parents)
  {
    boolean result = true;

    if ((node != null) && (node.getDependency() != null))
    {
      Artifact artifact = node.getDependency().getArtifact();

      if (artifact != null)
      {
        String id = getId(artifact);

        result = !getExludeSet().contains(id);

        if (!result && logger.isDebugEnabled())
        {
          logger.debug("exlcude dependency {} because it is blacklisted", id);
        }
      }
    }

    return result;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private Set<String> getExludeSet()
  {
    if (exludeSet == null)
    {
      try
      {
        exludeSet = loadExcludeSet();
      }
      catch (IOException ex)
      {
        throw Throwables.propagate(ex);
      }
    }

    return exludeSet;
  }

  /**
   * Method description
   *
   *
   * @param artifact
   *
   * @return
   */
  private String getId(Artifact artifact)
  {
    return artifact.getGroupId().concat(":").concat(artifact.getArtifactId());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<String> exludeSet;
}
