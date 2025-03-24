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

/**
 * This package holds classes for data migrations between different versions of SCM-Manager or its plugins. The central
 * classes for this are {@link sonia.scm.migration.UpdateStep} and {@link sonia.scm.migration.RepositoryUpdateStep}.
 * Implementations of these classes tell, what data they migrate
 * ({@link sonia.scm.migration.UpdateStepTarget#getAffectedDataType()}) and to what version this data type is migrated
 * ({@link sonia.scm.migration.UpdateStepTarget#getTargetVersion()}).
 * <p>The data type provided by {@link sonia.scm.migration.UpdateStepTarget#getAffectedDataType()} can be an arbitrary
 * string, but it is considered a best practice to use a qualified name, for example
 * <ul>
 * <li><code>com.example.myPlugin.configuration</code> for data in plugins, or</li>
 * <li><code>com.cloudogu.scm.repository</code> for core data structures.</li>
 * </ul>
 * <p>The version provided by {@link sonia.scm.migration.UpdateStepTarget#getTargetVersion()} is unrelated to other
 * versions and therefore can be chosen freely, so that a data type can be updated without in various ways independent
 * of other data types or the official version of the plugin or the core.
 * A coordination between different data types and their versions is only necessary, when update steps of different data
 * types rely on each other. If a update step of data type <i>A</i> has to run <b>before</b> another step for data type
 * <i>B</i>, the version number of the second step has to be greater in regards to
 * {@link sonia.scm.version.Version#compareTo(Version)}.
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
 * <li>their version number given by {@link sonia.scm.migration.UpdateStepTarget#getTargetVersion()} is bigger than the
 * last recorded target version of an executed update step for the data type given by
 * {@link sonia.scm.migration.UpdateStepTarget#getAffectedDataType()}, or
 * </li>
 * <li>there is no version number known for the given data type.
 * </li>
 * </ul>
 * These are the <i>relevant</i> update steps.
 * </li>
 * <li>These relevant update steps are then sorted ascending by their target version given by
 * {@link sonia.scm.migration.UpdateStepTarget#getTargetVersion()}.
 * </li>
 * <li>Finally, these sorted steps are executed one after another calling
 * {@link sonia.scm.migration.UpdateStep#doUpdate()} of each global step and
 * {@link sonia.scm.migration.RepositoryUpdateStep#doUpdate(sonia.scm.migration.RepositoryUpdateContext)} for each
 * repository for repository specific update steps, updating the version for the data type accordingly (if there are
 * both global and repository specific update steps for a data type and target version, the global step is called
 * first).
 * </li>
 * <li>If all works well, SCM-Manager then creates the runtime guice context by loading all further modules.</li>
 * <li>If any of the update steps fails, the whole process is interrupted and SCM-Manager will not start up and will
 * not record the version number of this update step.
 * </li>
 * </ul>
 * <p>Mind that an implementation of this class has to be annotated with {@link sonia.scm.plugin.Extension}, so that the
 * step will be found. </p>
 */
package sonia.scm.migration;

import sonia.scm.version.Version;
