/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
