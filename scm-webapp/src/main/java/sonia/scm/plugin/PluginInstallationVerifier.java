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
