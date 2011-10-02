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



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.DiffViewer;
import sonia.scm.repository.PathNotFoundException;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RevisionNotFoundException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 *
 * @author Sebastian Sdorra
 */
public class DiffStreamingOutput implements StreamingOutput
{

  /** the logger for DiffStreamingOutput */
  private static final Logger logger =
    LoggerFactory.getLogger(DiffStreamingOutput.class);

  //~--- constructors ---------------------------------------------------------

  /**
   *   Constructs ...
   *
   *
   *   @param diffViewer
   *   @param revision
   *   @param path
   */
  public DiffStreamingOutput(DiffViewer diffViewer, String revision,
                             String path)
  {
    this.diffViewer = diffViewer;
    this.revision = revision;
    this.path = path;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param output
   *
   * @throws IOException
   * @throws WebApplicationException
   */
  @Override
  public void write(OutputStream output)
          throws IOException, WebApplicationException
  {
    try
    {
      diffViewer.getDiff(revision, path, output);
    }
    catch (PathNotFoundException ex)
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("could not find path {}", ex.getPath());
      }

      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    catch (RevisionNotFoundException ex)
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("could not find revision {}", ex.getRevision());
      }

      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    catch (RepositoryException ex)
    {
      logger.error("could not write content to page", ex);

      throw new WebApplicationException(ex,
                                        Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private DiffViewer diffViewer;

  /** Field description */
  private String path;

  /** Field description */
  private String revision;
}
