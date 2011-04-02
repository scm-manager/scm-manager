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

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    "<changeset><id>{rev}:{short}</id><author>{author|escape}</author><description>{desc|escape}</description><date>{date|isodatesec}</date></changeset>\n";

  /** the logger for HgChangesetViewer */
  private static final Logger logger =
    LoggerFactory.getLogger(HgChangesetViewer.class);

  /** Field description */
  public static final Pattern REGEX_DATE =
    Pattern.compile("<date>([^<]+)</date>");

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
        StringReader reader =
          new StringReader(getFixedOutput(result.getOutput()));
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
    catch (ParseException ex)
    {
      logger.error("could not parse changeset dates", ex);
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
   * @param output
   *
   * @return
   *
   * @throws ParseException
   */
  private String getFixedOutput(String output) throws ParseException
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    StringBuilder changesetLog = new StringBuilder("<changesets>");
    StringTokenizer st = new StringTokenizer(output, "\n");

    while (st.hasMoreElements())
    {
      String line = st.nextToken();
      Matcher m = REGEX_DATE.matcher(line);

      if (m.find())
      {
        String dateString = m.group(1);
        Date date = sdf.parse(dateString);

        line = m.replaceAll(
          "<date>".concat(Util.formatDate(date)).concat("</date>"));
      }

      changesetLog.append(line);
    }

    changesetLog.append("</changesets>");

    return changesetLog.toString();
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
