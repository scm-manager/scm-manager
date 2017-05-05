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

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

/**
 * Unit tests for {@link InstanceIDFix}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class InstanceIDFixTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Test
  public void testIsRequired() throws SVNException, IOException {
    // svnkit uses db format 4 as default, so NONE must not be fixed
    assertFalse(createFix(Compatibility.NONE).isRequired());

    assertFalse(createFix(Compatibility.PRE14).isRequired());
    assertFalse(createFix(Compatibility.PRE15).isRequired());
    assertFalse(createFix(Compatibility.PRE16).isRequired());

    // WITH17 creates db format 7, which is subversion >= 1.9 or not?
    assertTrue(createFix(Compatibility.WITH17).isRequired());
  }

  @Test
  public void testAddInstanceID() throws SVNException, IOException {
    InstanceIDFix fix = createFix(Compatibility.WITH17);
    assertTrue(fix.isRequired());

    String instanceID = fix.addInstanceID();
    assertFalse(fix.isRequired());

    fix = new InstanceIDFix(fix.getRepository());
    assertFalse(fix.isRequired());
    assertTrue(fix.getUuids().contains(instanceID));
  }
  
  @Test(expected = IllegalStateException.class)
  public void testAddInstanceIDAllreadyFixedRepository() throws SVNException, IOException {
    InstanceIDFix fix = createFix(Compatibility.WITH17);
    assertTrue(fix.isRequired());

    fix.addInstanceID();
    fix.addInstanceID();
  }

  private InstanceIDFix createFix(Compatibility compatibility) throws SVNException, IOException {
    return new InstanceIDFix(createRepository(compatibility));
  }

  private File createRepository(Compatibility compatibility) throws SVNException, IOException {
    File directory = tempFolder.newFolder();
    SVNRepositoryFactory.createLocalRepository(directory, null, true, false, compatibility.isPre14Compatible(),
            compatibility.isPre15Compatible(), compatibility.isPre16Compatible(),
            compatibility.isPre17Compatible(), compatibility.isWith17Compatible());
    return directory;
  }
}
