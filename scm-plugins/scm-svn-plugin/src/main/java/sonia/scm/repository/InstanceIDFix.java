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


package sonia.scm.repository;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fixes Subversion repositories with db format 7, but without an instance id in the db/uuid file.
 * 
 * @see <a href="https://goo.gl/c3Ih89">910</a>
 * @author Sebastian Sdorra
 * @since 1.52
 */
public final class InstanceIDFix {
  
  private static final Logger LOG = LoggerFactory.getLogger(InstanceIDFix.class);
  
  private static final String PATH_DB = "db";
  private static final String PATH_FORMAT = "format";
  private static final String PATH_UUID = "uuid";
  
  private static final String DB_FORMAT = "7";
  private static final Charset CHARSET = Charsets.US_ASCII;
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  
  private final File repository;
  private final String dbFormat;
  private final List<String> uuids;
  
  /**
   * Creates a new instance to fix subversion repositories.
   * 
   * @param repository root directory of subversion repository
   * 
   * @throws IOException 
   */
  public InstanceIDFix(File repository) throws IOException {
    this.repository = repository;
    this.dbFormat = readDBFormat(repository);
    this.uuids = readUUIDS(repository);
  }
  
  /**
   * Returns {@core true} if the repository format is 7 and the instance id is missing.
   * 
   * @return {@core true} if the repository must be fixed.
   */
  public boolean isRequired() {
    return DB_FORMAT.equals(dbFormat) && uuids.size() == 1;
  }

  private String readDBFormat(File directory) throws IOException {
    return Files.readFirstLine(dbFile(directory, PATH_FORMAT), CHARSET);
  }
  
  private File dbFile(File directory, String filename) {
    return new File(directory, PATH_DB + File.separator + filename);
  }
  
  private List<String> readUUIDS(File directory) throws IOException {
    return new ArrayList<>(Files.readLines(dbFile(directory, PATH_UUID), CHARSET));
  }
  
  /**
   * Add missing instance id to the uuid file of the repository and returns the generated instance id.
   * 
   * @throws IOException
   * @return generated instance id
   */
  public String addInstanceID() throws IOException {
    Preconditions.checkState(isRequired(), "repository has already an instance id and does not require the fix");
    String uuid = uuids.get(0);
    String instanceID = generateInstanceID();
    addInstanceID(uuid, instanceID);
    return instanceID;
  }
  
  private void addInstanceID(String uuid, String instanceID) throws IOException {
    LOG.info("created instance id {} for subversion repository {} format 7", instanceID, uuid);
    String uuidFileContent = createUUIDFileContent(uuid, instanceID);
    Files.write(uuidFileContent, dbFile(repository, PATH_UUID), CHARSET);
    uuids.add(instanceID);
  }
  
  private String generateInstanceID() {
    return UUID.randomUUID().toString();
  }
  
  private String createUUIDFileContent(String uuid, String instanceID) {
    return new StringBuilder(uuid)
            .append(LINE_SEPARATOR)
            .append(instanceID)
            .append(LINE_SEPARATOR)
            .toString();
    
  }
  
  @VisibleForTesting
  List<String> getUuids() {
    return ImmutableList.copyOf(uuids);
  }

  @VisibleForTesting
  File getRepository() {
    return repository;
  }
}
