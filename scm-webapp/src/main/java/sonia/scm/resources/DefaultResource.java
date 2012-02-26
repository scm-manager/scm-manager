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


package sonia.scm.resources;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.ChecksumUtil;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.List;

import javax.servlet.ServletContext;

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultResource extends AbstractResource
{

  /**
   * Constructs ...
   *
   *
   * @param servletContext
   * @param resources
   * @param resourceHandlers
   * @param type
   *
   * @throws IOException
   */
  public DefaultResource(ServletContext servletContext, List<String> resources,
                         List<ResourceHandler> resourceHandlers,
                         ResourceType type)
          throws IOException
  {
    super(servletContext, resources, resourceHandlers);
    this.type = type;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try
    {
      appendResources(baos);
      this.content = baos.toByteArray();
      this.name = ChecksumUtil.createChecksum(this.content).concat(".").concat(
        type.getExtension());
    }
    finally
    {
      IOUtil.close(baos);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param output
   *
   * @throws IOException
   */
  @Override
  public void copyTo(OutputStream output) throws IOException
  {
    output.write(content);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getName()
  {
    return name;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public ResourceType getType()
  {
    return type;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private byte[] content;

  /** Field description */
  private String name;

  /** Field description */
  private ResourceType type;
}
