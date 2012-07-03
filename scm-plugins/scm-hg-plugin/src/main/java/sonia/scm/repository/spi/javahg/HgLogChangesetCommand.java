/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.spi.javahg;

//~--- non-JDK imports --------------------------------------------------------

import com.aragost.javahg.DateTime;
import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.flags.LogCommandFlags;
import com.aragost.javahg.internals.AbstractCommand;
import com.aragost.javahg.internals.HgInputStream;
import com.aragost.javahg.internals.RuntimeIOException;
import com.aragost.javahg.internals.Utils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Person;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgLogChangesetCommand extends AbstractCommand
{

  /**
   * Character sequence that indicate the begin and end of the
   * changeset in the command output.
   * <p>
   * This pattern is written by the CHANGESET_STYLE_PATH
   */
  private static final byte[] CHANGESET_PATTERN = Utils.randomBytes();

  /** Field description */
  private static final String CHANGESET_EAGER_STYLE_PATH =
    Utils.resourceAsFile("/styles/changesets-eager.style",
      ImmutableMap.of("pattern", CHANGESET_PATTERN)).getPath();

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repository
   */
  public HgLogChangesetCommand(Repository repository)
  {
    super(repository);
    withDebugFlag();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param files
   *
   * @return
   */
  public List<Changeset> execute(String... files)
  {
    cmdAppend("--style", CHANGESET_EAGER_STYLE_PATH);

    HgInputStream stream = launchStream(files);

    return readListFromStream(stream);
  }

  /**
   * Method description
   *
   *
   * @param limit
   *
   * @return
   */
  public HgLogChangesetCommand limit(int limit)
  {
    cmdAppend("-l", limit);

    return this;
  }

  /**
   * Method description
   *
   *
   * @param rev
   *
   * @return
   */
  public HgLogChangesetCommand rev(String... rev)
  {
    cmdAppend("-r", rev);

    return this;
  }

  /**
   * Method description
   *
   *
   * @param files
   *
   * @return
   */
  public Changeset single(String... files)
  {
    return Utils.single(execute(files));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getCommandName()
  {
    return "log";
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param in
   *
   * @return
   *
   * @throws IOException
   */
  private Changeset createFromInputStream(HgInputStream in) throws IOException
  {
    Changeset changeset = new Changeset();
    byte[] node = in.next(40);
    String nodeString = new String(node);

    changeset.setId(nodeString);

    // revision
    in.revisionUpTo('\n');

    String user = in.textUpTo('\n');

    changeset.setAuthor(Person.toPerson(user));

    DateTime timestamp = in.dateTimeUpTo('\n');

    changeset.setDate(timestamp.getDate().getTime());

    String branch = in.textUpTo('\n');

    changeset.getBranches().add(branch);

    // in.upTo(':');

    String p1 = in.nextAsText(40);

    if (!Strings.isNullOrEmpty(p1))
    {
      changeset.getParents().add(p1);
    }

    in.upTo(':');

    String p2 = in.nextAsText(40);

    if (!Strings.isNullOrEmpty(p2))
    {
      changeset.getParents().add(p2);
    }

    in.mustMatch(' ');    // skip space part of {parents}

    Modifications modifications = changeset.getModifications();

    String line = in.textUpTo('\n');

    while (line.length() > 0)
    {
      if (line.startsWith("a "))
      {
        modifications.getAdded().add(line.substring(2));
      }
      else if (line.startsWith("m "))
      {
        modifications.getModified().add(line.substring(2));
      }
      else if (line.startsWith("d "))
      {
        modifications.getRemoved().add(line.substring(2));
      }
      else if (line.startsWith("t "))
      {
        changeset.getTags().add(line.substring(2));
      }

      line = in.textUpTo('\n');
    }

    String message = in.textUpTo('\0');

    changeset.setDescription(message);

    return changeset;
  }

  /**
   * Method description
   *
   *
   * @param in
   *
   * @return
   */
  private List<Changeset> readListFromStream(HgInputStream in)
  {
    List<Changeset> changesets = Lists.newArrayList();

    try
    {
      boolean found = in.find(CHANGESET_PATTERN);

      if (found)
      {
        while (!in.match(CHANGESET_PATTERN))
        {

          Changeset cset = createFromInputStream(in);

          if (cset != null)
          {
            changesets.add(cset);
          }
        }

        Utils.consumeAll(in);
      }

      // If the pattern is not found there is no changsets
    }
    catch (IOException e)
    {
      throw new RuntimeIOException(e);
    }

    return changesets;
  }
}
