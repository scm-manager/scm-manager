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

import sonia.scm.io.Command;
import sonia.scm.io.CommandResult;
import sonia.scm.io.SimpleCommand;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgChangesetViewer implements ChangesetViewer
{

  /** Field description */
  public static final String ID_TIP = "tip";

  /** Field description */
  public static final String TEMPLATE =
    "<changeset><id>{rev}:{short}</id><author>{author|escape}</author><description>{desc|escape}</description><date>{date|isodatesec}</date></changeset>";

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
    this.handler = handler;
    this.repository = repository;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * TODO unmarshall date
   *
   *
   * @param startId
   * @param max
   *
   * @return
   */
  @Override
  public List<Changeset> getChangesets(String startId, int max)
  {
    List<Changeset> changesets = null;
    InputStream in = null;

    try
    {
      String repositoryPath = getRepositoryPath(repository);
      Command command = new SimpleCommand(handler.getConfig().getHgBinary(),
                          "-R", repositoryPath, "log", "-r", "tip:0", "-l",
                          String.valueOf(max), "--template", TEMPLATE);
      CommandResult result = command.execute();

      if (result.isSuccessfull())
      {
        StringBuilder changesetLog = new StringBuilder("<changesets>");

        changesetLog.append(result.getOutput());
        changesetLog.append("</changesets>");

        StringReader reader = new StringReader(changesetLog.toString());
        Unmarshaller unmarshaller = handler.createChangesetUnmarshaller();
        Changesets cs = (Changesets) unmarshaller.unmarshal(reader);

        if ((cs != null) && Util.isNotEmpty(cs.changesets))
        {
          changesets = cs.changesets;
        }
        else if (logger.isWarnEnabled())
        {
          logger.warn("could not find any changeset from {} to +{}", startId,
                      max);
        }
      }
      else
      {
        logger.error("could not load changesets, hg return code: {}\n{}",
                     result.getReturnCode(), result.getOutput());
      }
    }
    catch (IOException ex)
    {
      logger.error("could not load changesets", ex);
    }
    catch (JAXBException ex)
    {
      logger.error("could not unmarshall changesets", ex);
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
   * @param max
   *
   * @return
   */
  @Override
  public List<Changeset> getLastChangesets(int max)
  {
    return getChangesets(ID_TIP, max);
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private String getRepositoryPath(Repository repository)
  {
    return handler.getDirectory(repository).getAbsolutePath();
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   * @author         Sebastian Sdorra
   */
  @XmlRootElement(name = "changesets")
  private static class Changesets
  {

    /** Field description */
    @XmlElement(name = "changeset")
    private List<Changeset> changesets;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private Repository repository;
}
