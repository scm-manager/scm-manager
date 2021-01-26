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

package sonia.scm.migration;

import sonia.scm.plugin.ExtensionPoint;
import sonia.scm.version.Version;

/**
 * This is the main interface for data migration/update. Using this interface, SCM-Manager provides the possibility to
 * change data structures between versions for a given type of data.
 * <p>The data type can be an arbitrary string, but it is considered a best practice to use a qualified name, for
 * example
 * <ul>
 * <li><code>com.example.myPlugin.configuration</code></li> for data in plugins, or
 * <li><code>com.cloudogu.scm.repository</code></li> for core data structures.
 * </ul>
 * </p>
 * <p>The version is unrelated to other versions and therefore can be chosen freely, so that a data type can be updated
 * without in various ways independent of other data types or the official version of the plugin or the core.
 * A coordination between different data types and their versions is only necessary, when update steps of different data
 * types rely on each other. If a update step of data type <i>A</i> has to run <b>before</b> another step for data type
 * <i>B</i>, the version number of the second step has to be greater in regards to {@link Version#compareTo(Version)}.
 * </p>
 * <p>The algorithm looks something like this:<br>
 * Whenever the SCM-Manager starts,
 * <ul>
 * <li>it creates a so called <i>bootstrap guice context</i>, that contains
 * <ul>
 * <li>a {@link sonia.scm.security.KeyGenerator},</li>
 * <li>the {@link sonia.scm.repository.RepositoryLocationResolver},</li>
 * <li>an {@link sonia.scm.update.RepositoryUpdateIterator},</li>
 * <li>the {@link sonia.scm.io.FileSystem},</li>
 * <li>the {@link sonia.scm.security.CipherHandler},</li>
 * <li>a {@link sonia.scm.store.ConfigurationStoreFactory},</li>
 * <li>a {@link sonia.scm.store.ConfigurationEntryStoreFactory},</li>
 * <li>a {@link sonia.scm.store.DataStoreFactory},</li>
 * <li>a {@link sonia.scm.store.BlobStoreFactory}, and</li>
 * <li>the {@link sonia.scm.plugin.PluginLoader}.</li>
 * </ul>
 * Mind, that there are no DAOs, Managers or the like available at this time!
 * </li>
 * <li>It then checks whether there are instances of this interface that have not run before, that is either
 * <ul>
 * <li>their version number given by {@link #getTargetVersion()} is bigger than the last recorded target version of an
 * executed update step for the data type given by {@link #getAffectedDataType()}, or
 * </li>
 * <li>there is no version number known for the given data type.
 * </li>
 * </ul>
 * These are the <i>relevant</i> update steps.
 * </li>
 * <li>These relevant update steps are then sorted ascending by their target version given by
 * {@link #getTargetVersion()}.
 * </li>
 * <li>Finally, these sorted steps are executed one after another calling {@link #doUpdate()} of each step, updating the
 * version for the data type accordingly.
 * </li>
 * <li>If all works well, SCM-Manager then creates the runtime guice context by loading all further modules.</li>
 * <li>If any of the update steps fails, the whole process is interrupted and SCM-Manager will not start up and will
 * not record the version number of this update step.
 * </li>
 * </ul>
 * </p>
 * <p>Mind that an implementation of this class has to be annotated with {@link sonia.scm.plugin.Extension}, so that the
 * step will be found. </p>
 */
@ExtensionPoint
public interface UpdateStep {
  /**
   * Implement this to update the data to the new version. If any {@link Exception} is thrown, SCM-Manager will not
   * start up.
   */
  void doUpdate() throws Exception;

  /**
   * Declares the new version of the data type given by {@link #getAffectedDataType()}. A update step will only be
   * executed, when this version is bigger than the last recorded version for its data type according to
   * {@link Version#compareTo(Version)}
   */
  Version getTargetVersion();

  /**
   * Declares the data type this update step will take care of. This should be a qualified name, like
   * <code>com.example.myPlugin.configuration</code>.
   */
  String getAffectedDataType();
}
