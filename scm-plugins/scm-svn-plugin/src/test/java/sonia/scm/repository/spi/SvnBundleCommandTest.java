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

package sonia.scm.repository.spi;


import com.google.common.io.ByteSink;
import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.repository.api.BundleResponse;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class SvnBundleCommandTest extends AbstractSvnCommandTestBase
{

  @Test
  public void testBundle() throws IOException
  {
    File file = temp.newFile();
    ByteSink sink = Files.asByteSink(file);
    BundleCommandRequest req = new BundleCommandRequest(sink);
    BundleResponse res = new SvnBundleCommand(createContext()).bundle(req);

    assertThat(res, notNullValue());
    assertThat(res.getChangesetCount(), is(5l));
    assertTrue("file does not exists", file.exists());
    assertThat(file.length(), greaterThan(0l));
  }

  //~--- fields ---------------------------------------------------------------

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();
}
