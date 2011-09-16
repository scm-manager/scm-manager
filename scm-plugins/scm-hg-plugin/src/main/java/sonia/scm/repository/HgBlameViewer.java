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

import sonia.scm.util.AssertUtil;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgBlameViewer implements BlameViewer
{

  /** the logger for HgBlameViewer */
  private static final Logger logger =
    LoggerFactory.getLogger(HgBlameViewer.class);

  // sample: "Sebastian Sdorra <s.sdorra@ostfalia.de>  34  Wed Sep 08 10:22:46 2010 +0200:1: <?xml version=\"1.0\" encoding=\"UTF-8\"?>"

  /** Field description */
  public static final Pattern REGEX_FIRSTPART =
    Pattern.compile(
        "(?i)^(.*)\\s+([0-9]+)\\s+([a-z]{3}\\s[a-z]{3}\\s[0-9]{2}\\s[0-9]{2}:[0-9]{2}:[0-9]{2}\\s[0-9]{4}\\s[+-][0-9]{4}):([0-9]+):\\s(.*)$");

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param repositoryDirectory
   */
  public HgBlameViewer(HgRepositoryHandler handler, File repositoryDirectory)
  {
    this.handler = handler;
    this.repositoryDirectory = repositoryDirectory;
  }

  /**
   * Constructs ...
   *
   *
   *
   * @param handler
   * @param repository
   */
  public HgBlameViewer(HgRepositoryHandler handler, Repository repository)
  {
    this(handler, handler.getDirectory(repository));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param revision
   * @param path
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public BlameResult getBlame(String revision, String path)
          throws IOException
  {
    AssertUtil.assertIsNotEmpty(path);

    if (Util.isEmpty(revision))
    {
      revision = "tip";
    }

    ProcessBuilder builder =
      new ProcessBuilder(handler.getConfig().getHgBinary(), "annotate", "-r",
                         revision, "-dnulv", Util.nonNull(path));

    if (logger.isDebugEnabled())
    {
      StringBuilder msg = new StringBuilder();

      for (String param : builder.command())
      {
        msg.append(param).append(" ");
      }

      logger.debug(msg.toString());
    }

    Process p = builder.directory(repositoryDirectory).start();
    BufferedReader reader = null;
    BlameResult result = null;

    try
    {
      reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
      result = parseBlameInput(reader);
    }
    finally
    {
      IOUtil.close(reader);
    }

    return result;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param reader
   *
   * @return
   *
   * @throws IOException
   */
  private BlameResult parseBlameInput(BufferedReader reader)
          throws IOException
  {
    List<BlameLine> blameLines = new ArrayList<BlameLine>();
    String line = reader.readLine();

    while (line != null)
    {
      BlameLine blameLine = parseBlameLine(line);

      if (blameLine != null)
      {
        blameLines.add(blameLine);
      }

      line = reader.readLine();
    }

    return new BlameResult(blameLines.size(), blameLines);
  }

  /**
   * Method description
   *
   *
   * @param line
   *
   * @return
   */
  private BlameLine parseBlameLine(String line)
  {
    BlameLine blameLine = null;
    Matcher m = REGEX_FIRSTPART.matcher(line);

    if (m.matches())
    {
      Person authorPerson = Person.toPerson(m.group(1));

      // todo parse date
      Long when = getDate(m.group(3));

      blameLine = new BlameLine(authorPerson, when, m.group(2), m.group(5),
                                Integer.parseInt(m.group(4)));
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("line '{}' does not match", line);
    }

    return blameLine;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param dateString
   *
   * @return
   */
  private Long getDate(String dateString)
  {
    Long date = null;

    // Wed Sep 08 10:22:46 2010 +0200
    try
    {
      SimpleDateFormat sdf =
        new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy zzzz", Locale.ENGLISH);

      date = sdf.parse(dateString).getTime();
    }
    catch (ParseException ex)
    {
      logger.warn("could not parse date string ".concat(dateString), ex);
    }

    return date;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private File repositoryDirectory;
}
