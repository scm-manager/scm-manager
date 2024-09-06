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

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import jakarta.inject.Inject;
import sonia.scm.SCMContextProvider;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static sonia.scm.plugin.Tracing.SPAN_KIND;

@SuppressWarnings("UnstableApiUsage") // guava hash is marked as unstable
class PluginInstaller {

  private final SCMContextProvider scmContext;
  private final AdvancedHttpClient client;
  private final PluginCenterAuthenticator authenticator;
  private final SmpDescriptorExtractor smpDescriptorExtractor;

  @Inject
  public PluginInstaller(SCMContextProvider scmContext, AdvancedHttpClient client, PluginCenterAuthenticator authenticator, SmpDescriptorExtractor smpDescriptorExtractor) {
    this.scmContext = scmContext;
    this.client = client;
    this.authenticator = authenticator;
    this.smpDescriptorExtractor = smpDescriptorExtractor;
  }

  @SuppressWarnings("squid:S4790") // hashing should be safe
  public PendingPluginInstallation install(PluginInstallationContext context, AvailablePlugin plugin) {
    Path file = null;
    try (HashingInputStream input = new HashingInputStream(Hashing.sha256(), download(plugin))) {
      file = createFile(plugin);
      Files.copy(input, file);

      verifyChecksum(plugin, input.hash(), file);

      InstalledPluginDescriptor descriptor = smpDescriptorExtractor.extractPluginDescriptor(file);
      verifyInformation(plugin.getDescriptor(), descriptor);

      PluginInstallationVerifier.verify(context, descriptor);

      return new PendingPluginInstallation(plugin.install(), file);
    } catch (PluginException ex) {
      cleanup(file);
      throw ex;
    } catch (IOException ex) {
      cleanup(file);
      throw new PluginDownloadException(plugin, ex);
    }
  }

  private void verifyInformation(AvailablePluginDescriptor descriptorFromPluginCenter, InstalledPluginDescriptor downloadedDescriptor) {
    verifyInformation(descriptorFromPluginCenter.getInformation(), downloadedDescriptor.getInformation());
  }

  private void verifyInformation(PluginInformation informationFromPluginCenter, PluginInformation downloadedInformation) {
    if (!informationFromPluginCenter.getName().equals(downloadedInformation.getName())) {
      throw new PluginInformationMismatchException(
        informationFromPluginCenter, downloadedInformation,
        String.format(
          "downloaded plugin name \"%s\" does not match the expected name \"%s\" from plugin-center",
          downloadedInformation.getName(),
          informationFromPluginCenter.getName()
        )
      );
    }
    if (!informationFromPluginCenter.getVersion().equals(downloadedInformation.getVersion())) {
      throw new PluginInformationMismatchException(
        informationFromPluginCenter, downloadedInformation,
        String.format(
          "downloaded plugin version \"%s\" does not match the expected version \"%s\" from plugin-center",
          downloadedInformation.getVersion(),
          informationFromPluginCenter.getVersion()
        )
      );
    }
  }

  private void cleanup(Path file) {
    try {
      if (file != null) {
        Files.deleteIfExists(file);
      }
    } catch (IOException e) {
      throw new PluginCleanupException(file);
    }
  }

  private void verifyChecksum(AvailablePlugin plugin, HashCode hash, Path file) {
    Optional<String> checksum = plugin.getDescriptor().getChecksum();
    if (checksum.isPresent()) {
      String calculatedChecksum = hash.toString();
      if (!checksum.get().equalsIgnoreCase(calculatedChecksum)) {
        cleanup(file);
        throw new PluginChecksumMismatchException(plugin, calculatedChecksum, checksum.get());
      }
    }
  }

  private InputStream download(AvailablePlugin plugin) throws IOException {
    AdvancedHttpRequest request = client.get(plugin.getDescriptor().getUrl()).spanKind(SPAN_KIND);
    if (authenticator.isAuthenticated()) {
      authenticator.fetchAccessToken().ifPresent(request::bearerAuth);
    }
    return request.request().contentAsStream();
  }

  private Path createFile(AvailablePlugin plugin) throws IOException {
    Path directory = scmContext.resolve(Paths.get("plugins"));
    Files.createDirectories(directory);
    return directory.resolve(plugin.getDescriptor().getInformation().getName() + ".smp");
  }
}
