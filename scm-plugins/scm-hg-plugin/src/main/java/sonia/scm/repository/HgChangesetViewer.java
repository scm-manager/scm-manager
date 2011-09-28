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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import sonia.scm.io.Command;
import sonia.scm.io.CommandResult;
import sonia.scm.io.SimpleCommand;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgChangesetViewer implements ChangesetViewer
{

  /** Field description */
  public static final String ID_TIP = "tip";

  /** Field description */
  //J-
  public static final String TEMPLATE_CHANGESETS =
        "\"<changeset>"
      +   "<id>{rev}:{node|short}</id>"
      +   "<author>{author|escape}</author>"
      +   "<description>{desc|escape}</description>"
      +   "<date>{date|isodatesec}</date>"
      +   "<tags>{tags}</tags>"
      +   "<branches>{branches}</branches>"
      +   "<files-added>{file_adds}</files-added>"
      +   "<files-mods>{file_mods}</files-mods>"
      +   "<files-dels>{file_dels}</files-dels>"
      + "</changeset>\"";
  //J+

  /** Field description */
  public static final String ENV_PENDING = "HG_PENDING";

  /** Field description */
  public static final String TEMPLATE_TOTAL = "{rev}";

  /** the logger for HgChangesetViewer */
  private static final Logger logger =
    LoggerFactory.getLogger(HgChangesetViewer.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param handler
   * @param repository
   */
  public HgChangesetViewer(HgRepositoryHandler handler, Repository repository)
  {
    this(handler, handler.getDirectory(repository).getAbsolutePath());
  }

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param repositoryPath
   */
  public HgChangesetViewer(HgRepositoryHandler handler, String repositoryPath)
  {
    this.handler = handler;
    this.repositoryPath = repositoryPath;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   *
   *
   * @param start
   * @param max
   *
   * @return
   */
  @Override
  public ChangesetPagingResult getChangesets(int start, int max)
  {
    ChangesetPagingResult changesets = null;
    InputStream in = null;

    try
    {
      int total = getTotalChangesets(repositoryPath);
      int startRev = total - start;
      int endRev = total - start - (max - 1);

      if (endRev < 0)
      {
        endRev = 0;
      }

      List<Changeset> changesetList = getChangesets(Integer.toString(startRev),
                                        Integer.toString(endRev));

      if (changesetList != null)
      {
        if (total == -1)
        {
          total = 0;
        }
        else
        {
          total++;
        }

        changesets = new ChangesetPagingResult(total, changesetList);
      }
    }
    catch (IOException ex)
    {
      logger.error("could not load changesets", ex);
    }
    finally
    {
      IOUtil.close(in);
    }

    return changesets;
  }

  /**
   * Method description
   *
   *
   * @param startRev
   * @param endRev
   * @param pending
   *
   * @return
   *
   * @throws IOException
   */
  public List<Changeset> getChangesets(String startRev, String endRev,
          boolean pending)
          throws IOException
  {
    List<Changeset> changesetList = null;
    InputStream in = null;

    try
    {
      SimpleCommand command =
        new SimpleCommand(handler.getConfig().getHgBinary(), "-R",
                          repositoryPath, "log", "-r", startRev + ":" + endRev,
                          "--template", TEMPLATE_CHANGESETS);

      if (pending)
      {
        Map<String, String> env = new HashMap<String, String>();

        env.put(ENV_PENDING, repositoryPath);
        command.setEnvironment(env);
      }

      CommandResult result = command.execute();

      if (result.isSuccessfull())
      {
        StringBuilder sb = new StringBuilder("<changesets>");

        sb.append(result.getOutput()).append("</changesets>");
        changesetList = new HgChangesetParser().parse(
          new InputSource(new StringReader(sb.toString())));
      }
      else if (logger.isErrorEnabled())
      {
        logger.error(
            "command for fetching changesets failed with exit code {} and output {}",
            result.getReturnCode(), result.getOutput());
      }
    }
    catch (ParserConfigurationException ex)
    {
      logger.error("could not parse changesets", ex);
    }
    catch (SAXException ex)
    {
      logger.error("could not unmarshall changesets", ex);
    }
    finally
    {
      IOUtil.close(in);
    }

    return changesetList;
  }

  /**
   * Method description
   *
   *
   * @param startRev
   * @param endRev
   *
   * @return
   *
   * @throws IOException
   */
  public List<Changeset> getChangesets(String startRev, String endRev)
          throws IOException
  {
    return getChangesets(startRev, endRev, false);
  }

  /**
   * Method description
   *
   *
   * @param repositoryPath
   *
   * @return
   *
   * @throws IOException
   */
  private int getTotalChangesets(String repositoryPath) throws IOException
  {
    int total = -1;
    Command command = new SimpleCommand(handler.getConfig().getHgBinary(),
                        "-R", repositoryPath, "tip", "--template",
                        TEMPLATE_TOTAL);
    CommandResult result = command.execute();

    if (result.isSuccessfull())
    {
      total = Integer.parseInt(result.getOutput().trim());
    }
    else if (logger.isErrorEnabled())
    {
      logger.error(
          "could not read tip revision, command returned with exit code {} and content {}",
          result.getReturnCode(), result.getOutput());
    }

    return total;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private String repositoryPath;
}
