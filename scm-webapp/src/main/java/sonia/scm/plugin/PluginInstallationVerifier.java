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

package sonia.scm.plugin;

import sonia.scm.version.Version;

import java.util.Optional;
import java.util.Set;

public final class PluginInstallationVerifier {

  private final PluginInstallationContext context;
  private final InstalledPluginDescriptor descriptor;

  private PluginInstallationVerifier(PluginInstallationContext context, InstalledPluginDescriptor descriptor) {
    this.context = context;
    this.descriptor = descriptor;
  }

  public static void verify(PluginInstallationContext context, InstalledPlugin plugin) {
    verify(context, plugin.getDescriptor());
  }

  public static void verify(PluginInstallationContext context, InstalledPluginDescriptor descriptor) {
    new PluginInstallationVerifier(context, descriptor).doVerification();
  }

  private void doVerification() {
    verifyConditions();
    verifyDependencies();
    verifyOptionalDependencies();
  }

  private void verifyConditions() {
    // TODO we should provide more details here, which condition has failed

    PluginCondition pluginCondition =descriptor.getCondition();
    PluginCondition.CheckResult pluginConditionCheckResult = pluginCondition.getConditionCheckResult();
    if (!pluginConditionCheckResult.equals(PluginCondition.CheckResult.OK)) {
      throw new PluginConditionFailedException(descriptor.getInformation().getName(), pluginCondition);
    }
  }

  private void verifyDependencies() {
    Set<NameAndVersion> dependencies = descriptor.getDependenciesWithVersion();
    for (NameAndVersion dependency : dependencies) {
      NameAndVersion installed = context.find(dependency.getName())
        .orElseThrow(
          () -> new DependencyNotFoundException(descriptor.getInformation().getName(), dependency.getName())
        );

      dependency.getVersion().ifPresent(requiredVersion -> verifyDependencyVersion(dependency, installed));
    }
  }

  private void verifyOptionalDependencies() {
    Set<NameAndVersion> dependencies = descriptor.getOptionalDependenciesWithVersion();
    for (NameAndVersion dependency : dependencies) {
      Optional<Version> version = dependency.getVersion();
      if (version.isPresent()) {
        Optional<NameAndVersion> installed = context.find(dependency.getName());
        installed.ifPresent(nameAndVersion -> verifyDependencyVersion(dependency, nameAndVersion));
      }
    }
  }

  private void verifyDependencyVersion(NameAndVersion required, NameAndVersion installed) {
    Version requiredVersion = required.mustGetVersion();
    Version installedVersion = installed.mustGetVersion();
    if (installedVersion.isOlder(requiredVersion)) {
      throw new DependencyVersionMismatchException(
        descriptor.getInformation().getName(),
        required.getName(),
        requiredVersion.getUnparsedVersion(),
        installedVersion.getUnparsedVersion()
      );
    }
  }
}
