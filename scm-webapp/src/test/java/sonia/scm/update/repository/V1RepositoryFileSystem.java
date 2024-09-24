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

package sonia.scm.update.repository;

import sonia.scm.repository.spi.ZippedRepositoryTestBase;

import java.io.IOException;
import java.nio.file.Path;

class V1RepositoryFileSystem {
  /**
   * Creates the following v1 repositories in the temp dir:
   * <pre>
   * <repository>
   *     <properties/>
   *     <contact>arthur@dent.uk</contact>
   *     <creationDate>1558423492071</creationDate>
   *     <description>A repository with two folders.</description>
   *     <id>3b91caa5-59c3-448f-920b-769aaa56b761</id>
   *     <name>one/directory</name>
   *     <public>false</public>
   *     <archived>false</archived>
   *     <type>git</type>
   * </repository>
   * <repository>
   *     <properties/>
   *     <contact>arthur@dent.uk</contact>
   *     <creationDate>1558423543716</creationDate>
   *     <description>A repository in deeply nested folders.</description>
   *     <id>c1597b4f-a9f0-49f7-ad1f-37d3aae1c55f</id>
   *     <name>some/more/directories/than/one</name>
   *     <public>false</public>
   *     <archived>true</archived>
   *     <type>git</type>
   * </repository>
   * <repository>
   *     <properties/>
   *     <contact>arthur@dent.uk</contact>
   *     <creationDate>1558423440258</creationDate>
   *     <description>A simple repository without directories.</description>
   *     <id>454972da-faf9-4437-b682-dc4a4e0aa8eb</id>
   *     <lastModified>1558425918578</lastModified>
   *     <name>simple</name>
   *     <permissions>
   *         <groupPermission>true</groupPermission>
   *         <name>mice</name>
   *         <type>WRITE</type>
   *     </permissions>
   *     <permissions>
   *         <groupPermission>false</groupPermission>
   *         <name>dent</name>
   *         <type>OWNER</type>
   *     </permissions>
   *     <permissions>
   *         <groupPermission>false</groupPermission>
   *         <name>trillian</name>
   *         <type>READ</type>
   *     </permissions>
   *     <public>false</public>
   *     <archived>false</archived>
   *     <type>git</type>
   *     <url>http://localhost:8081/scm/git/simple</url>
   * </repository>
   * </pre>
   */
  static void createV1Home(Path tempDir) throws IOException {
    ZippedRepositoryTestBase.extract(tempDir.toFile(), "sonia/scm/update/repository/scm-home.v1.zip");
  }
}
