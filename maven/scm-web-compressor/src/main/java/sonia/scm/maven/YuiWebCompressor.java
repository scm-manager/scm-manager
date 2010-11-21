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



package sonia.scm.maven;

//~--- non-JDK imports --------------------------------------------------------

import com.yahoo.platform.yui.compressor.CssCompressor;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class YuiWebCompressor extends AbstractWebCompressor
{

  /** Field description */
  public static final String EXTENSION = "css";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param document
   * @param path
   */
  @Override
  protected void appendElement(Document document, String path)
  {
    document.appendElement("link").attr("type", "text/css").attr("rel",
                           "stylesheet").attr("href", path);
  }

  /**
   * Method description
   *
   *
   * @param source
   * @param target
   * @param encoding
   *
   * @throws IOException
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  @Override
  protected void compress(File source, File target, String encoding)
          throws IOException, MojoExecutionException, MojoFailureException
  {
    FileReader reader = null;
    FileWriter writer = null;

    try
    {
      reader = new FileReader(source);

      CssCompressor compressor = new CssCompressor(reader);

      writer = new FileWriter(target);
      compressor.compress(writer, 5000);
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
   * @param document
   *
   * @return
   */
  @Override
  protected Elements selectElements(Document document)
  {
    return document.select("link[type=text/css][href]");
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getExtension()
  {
    return EXTENSION;
  }

  /**
   * Method description
   *
   *
   * @param inputDirectory
   * @param element
   *
   * @return
   */
  @Override
  protected File getFile(File inputDirectory, Element element)
  {
    return new File(inputDirectory, element.attr("href"));
  }
}
