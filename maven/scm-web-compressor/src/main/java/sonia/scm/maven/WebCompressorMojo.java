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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class WebCompressorMojo extends AbstractMojo
{

  /**
   * Constructs ...
   *
   */
  public WebCompressorMojo()
  {
    compressorSet = new LinkedHashSet<WebCompressor>();
    compressorSet.add(new ClosureWebCompressor());
    compressorSet.add(new YuiWebCompressor());
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    OutputStream output = null;

    try
    {
      Document document = Jsoup.parse(inputFile, encoding);
      File inputDirectory = inputFile.getParentFile();

      for (WebCompressor compressor : compressorSet)
      {
        compressor.compress(document, inputDirectory, outputDirectory,
                            encoding, outputPrefix, true);
      }

      output = new FileOutputStream(outputFile);
      output.write(document.html().getBytes(encoding));
    }
    catch (IOException ex)
    {
      throw new MojoExecutionException(ex.getMessage(), ex);
    }
    finally
    {
      IOUtil.close(output);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getEncoding()
  {
    return encoding;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public File getInputFile()
  {
    return inputFile;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public File getOutputDirectory()
  {
    return outputDirectory;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public File getOutputFile()
  {
    return outputFile;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getOutputPrefix()
  {
    return outputPrefix;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param encoding
   */
  public void setEncoding(String encoding)
  {
    this.encoding = encoding;
  }

  /**
   * Method description
   *
   *
   * @param inputFile
   */
  public void setInputFile(File inputFile)
  {
    this.inputFile = inputFile;
  }

  /**
   * Method description
   *
   *
   * @param outputDirectory
   */
  public void setOutputDirectory(File outputDirectory)
  {
    this.outputDirectory = outputDirectory;
  }

  /**
   * Method description
   *
   *
   * @param outputFile
   */
  public void setOutputFile(File outputFile)
  {
    this.outputFile = outputFile;
  }

  /**
   * Method description
   *
   *
   * @param outputPrefix
   */
  public void setOutputPrefix(String outputPrefix)
  {
    this.outputPrefix = outputPrefix;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<WebCompressor> compressorSet;

  /**
   * @parameter
   */
  private String encoding = "UTF-8";

  /**
   * @parameter
   * @required
   */
  private File inputFile;

  /**
   * @parameter
   */
  private File outputDirectory;

  /**
   * @parameter
   * @required
   */
  private File outputFile;

  /**
   * @parameter
   * @required
   */
  private String outputPrefix;
}
