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

import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.Result;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class ClosureWebCompressor extends AbstractWebCompressor
{

  /** Field description */
  public static final String EXTENSION = "js";

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
    document.head().appendElement("script").attr("type",
                                  "text/javascript").attr("src", path);
  }

  /**
   * Method description
   *
   *
   * @param sourceFile
   * @param targetFile
   * @param encoding
   *
   * @throws IOException
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  @Override
  protected void compress(File sourceFile, File targetFile, String encoding)
          throws IOException, MojoExecutionException, MojoFailureException
  {
    com.google.javascript.jscomp.Compiler compiler =
      new com.google.javascript.jscomp.Compiler();
    CompilerOptions options = new CompilerOptions();
    final JSSourceFile extern = JSSourceFile.fromCode("externs.js",
                                  "function alert(x) {}");
    JSSourceFile source = JSSourceFile.fromFile(sourceFile);
    Result result = compiler.compile(extern, source, options);

    if (!result.success)
    {
      throw new MojoFailureException("compression failed");
    }
    else
    {
      FileOutputStream output = null;

      try
      {
        output = new FileOutputStream(targetFile);
        output.write(compiler.toSource().getBytes(encoding));
      }
      finally
      {
        IOUtil.close(output);
      }
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
    return document.select("script[type=text/javascript][src]");
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
    return new File(inputDirectory, element.attr("src"));
  }
}
