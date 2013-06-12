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



package sonia.scm.template;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.Reader;

/**
 * The {@link TemplateEngine} searches for {@link Template}s and prepares the
 * template for the rendering process.
 *
 * @author Sebastian Sdorra
 * @since 1.19
 *
 * @apiviz.uses sonia.scm.template.Template
 */
public interface TemplateEngine
{

  /**
   * Returns the template associated with the given path. The template engine
   * will search the template in the folder of the web application and in
   * the classpath. This method will return null,
   * if no template could be found for the given path.
   *
   *
   * @param templatePath path of the template
   *
   * @return template associated withe the given path or null
   *
   * @throws IOException
   */
  public Template getTemplate(String templatePath) throws IOException;

  /**
   * Creates a template of the given reader. Note some template implementations 
   * will cache the template by its id.
   *
   *
   * @param templateIdentifier id of the template
   * @param reader template reader
   *
   * @return template created from the reader
   *
   * @throws IOException
   * 
   * @since 1.22
   */
  public Template getTemplate(String templateIdentifier, Reader reader)
    throws IOException;

  /**
   * Returns the type of this template engine.
   *
   * @return type of template engine
   */
  public TemplateType getType();
}
