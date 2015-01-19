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



package sonia.scm.repository.spi.javahg;

//~--- non-JDK imports --------------------------------------------------------

import com.aragost.javahg.DateTime;
import com.aragost.javahg.Repository;
import com.aragost.javahg.internals.AbstractCommand;
import com.aragost.javahg.internals.HgInputStream;
import com.aragost.javahg.internals.RuntimeIOException;
import com.aragost.javahg.internals.Utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Person;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractChangesetCommand extends AbstractCommand
{

  /** Field description */
  private static final String BRANCH_DEFAULT = "default";

  /**
   * Character sequence that indicate the begin and end of the
   * changeset in the command output.
   * <p>
   * This pattern is written by the CHANGESET_STYLE_PATH
   */
  private static final byte[] CHANGESET_PATTERN = Utils.randomBytes();

  /** Field description */
  protected static final String CHANGESET_LAZY_STYLE_PATH =
    Utils.resourceAsFile("/sonia/scm/styles/changesets-lazy.style",
      ImmutableMap.of("pattern", CHANGESET_PATTERN)).getPath();

  /** Field description */
  protected static final String CHANGESET_EAGER_STYLE_PATH =
    Utils.resourceAsFile("/sonia/scm/styles/changesets-eager.style",
      ImmutableMap.of("pattern", CHANGESET_PATTERN)).getPath();

  /** Field description */
  private static final String NULL_ID =
    "0000000000000000000000000000000000000000";

  /** changeset property for closed branch */
  private static final String PROPERTY_CLOSE = "hg.close";

  /** changeset property for parent1 revision */
  private static final String PROPERTY_PARENT1_REVISION = "hg.p1.rev";

  /** changeset property for parent2 revision */
  private static final String PROPERTY_PARENT2_REVISION = "hg.p2.rev";

  /** changeset property for node revision */
  private static final String PROPERTY_REVISION = "hg.rev";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repository
   * @param config
   */
  AbstractChangesetCommand(Repository repository, HgConfig config)
  {
    super(repository);
    this.config = config;
    withDebugFlag();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param stream
   *
   * @return
   */
  protected List<Integer> loadRevisionsFromStream(HgInputStream stream)
  {
    List<Integer> revisions = Lists.newArrayList();

    try
    {
      while (stream.peek() != -1)
      {
        int rev = stream.revisionUpTo(' ');

        if (rev >= 0)
        {
          revisions.add(rev);
        }
      }

    }
    catch (IOException ex)
    {
      throw new RuntimeIOException(ex);
    }

    return revisions;
  }

  /**
   * Method description
   *
   *
   * @param in
   *
   * @return
   */
  protected List<Changeset> readListFromStream(HgInputStream in)
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

    changeset.setId(readId(in, changeset, PROPERTY_REVISION));

    String user = in.textUpTo('\n');

    changeset.setAuthor(Person.toPerson(user));

    DateTime timestamp = in.dateTimeUpTo('\n');

    changeset.setDate(timestamp.getDate().getTime());

    String branch = in.textUpTo('\n');

    if (!BRANCH_DEFAULT.equals(branch))
    {
      changeset.getBranches().add(branch);
    }

    String p1 = readId(in, changeset, PROPERTY_PARENT1_REVISION);

    if (!isNullId(p1))
    {
      changeset.getParents().add(p1);
    }

    in.mustMatch(' ');

    String p2 = readId(in, changeset, PROPERTY_PARENT2_REVISION);

    if (!isNullId(p2))
    {
      changeset.getParents().add(p2);
    }

    // skip space part of {parents}
    in.mustMatch(' ');

    // read extras
    String extraLine = in.textUpTo('\n');

    if (extraLine.contains("close=1"))
    {
      changeset.getProperties().put(PROPERTY_CLOSE, "true");
    }

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
   * @param changeset
   * @param propertyKey
   *
   * @return
   *
   * @throws IOException
   */
  private String readId(HgInputStream in, Changeset changeset,
    String propertyKey)
    throws IOException
  {
    if (config.isShowRevisionInId())
    {
      Integer rev = in.readDecimal();

      if ((rev != null) && (rev >= 0))
      {
        changeset.setProperty(propertyKey, String.valueOf(rev));
      }
    }

    in.upTo(':');

    return in.nextAsText(40);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  private boolean isNullId(String id)
  {
    return ((id != null) && id.equals("-1:".concat(NULL_ID)))
      || NULL_ID.equals(id);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgConfig config;
}
