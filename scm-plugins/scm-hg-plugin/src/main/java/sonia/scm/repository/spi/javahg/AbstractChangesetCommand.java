/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository.spi.javahg;


import org.javahg.DateTime;
import org.javahg.Repository;
import org.javahg.internals.AbstractCommand;
import org.javahg.internals.HgInputStream;
import org.javahg.internals.RuntimeIOException;
import org.javahg.internals.Utils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.Modification;
import sonia.scm.repository.Person;

import java.io.IOException;
import java.util.Collection;
import java.util.List;


public abstract class AbstractChangesetCommand extends AbstractCommand
{

  public static final String BRANCH_DEFAULT = "default";

  /**
   * Character sequence that indicate the begin and end of the
   * changeset in the command output.
   * <p>
   * This pattern is written by the CHANGESET_STYLE_PATH
   */
  private static final byte[] CHANGESET_PATTERN = Utils.randomBytes();

  public static final String CHANGESET_LAZY_STYLE_PATH =
    Utils.resourceAsFile("/sonia/scm/styles/changesets-lazy.style",
      ImmutableMap.of("pattern", CHANGESET_PATTERN)).getPath();

  protected static final String CHANGESET_EAGER_STYLE_PATH =
    Utils.resourceAsFile("/sonia/scm/styles/changesets-eager.style",
      ImmutableMap.of("pattern", CHANGESET_PATTERN)).getPath();

  private static final String NULL_ID =
    "0000000000000000000000000000000000000000";

  /** changeset property for closed branch */
  public static final String PROPERTY_CLOSE = "hg.close";

  /** changeset property for parent1 revision */
  private static final String PROPERTY_PARENT1_REVISION = "hg.p1.rev";

  /** changeset property for parent2 revision */
  private static final String PROPERTY_PARENT2_REVISION = "hg.p2.rev";

  /** changeset property for node revision */
  private static final String PROPERTY_REVISION = "hg.rev";

  private HgConfig config;
 
  AbstractChangesetCommand(Repository repository, HgConfig config)
  {
    super(repository);
    this.config = config;
    withDebugFlag();
  }



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


  private Changeset createFromInputStream(HgInputStream in) throws IOException
  {
    Changeset changeset = new Changeset();

    changeset.setId(readId(in, changeset, PROPERTY_REVISION));

    String user = in.textUpTo('\n');

    changeset.setAuthor(Person.toPerson(user));

    DateTime timestamp = in.dateTimeUpTo('\n');

    changeset.setDate(timestamp.getDate().getTime());

    String branch = in.textUpTo('\n');

    changeset.getBranches().add(branch);

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

    String line = in.textUpTo('\n');
    while (line.length() > 0) {
      if (line.startsWith("t "))
      {
        changeset.getTags().add(line.substring(2));
      }
      line = in.textUpTo('\n');
    }
    String message = in.textUpTo('\0');

    changeset.setDescription(message);

    return changeset;
  }

  protected Collection<Modification> readModificationsFromStream(HgInputStream in) {
    try {
      boolean found = in.find(CHANGESET_PATTERN);
      if (found) {
        while (!in.match(CHANGESET_PATTERN)) {
          return extractModifications(in);
        }
      }
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }
    return null;
  }

  private Collection<Modification> extractModifications(HgInputStream in) throws IOException {
    HgModificationParser hgModificationParser = new HgModificationParser();
    String line = in.textUpTo('\n');
    while (line.length() > 0) {
      hgModificationParser.addLine(line);
      line = in.textUpTo('\n');
    }
    return hgModificationParser.getModifications();
  }

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



  private boolean isNullId(String id)
  {
    return ((id != null) && id.equals("-1:".concat(NULL_ID)))
      || NULL_ID.equals(id);
  }

}
