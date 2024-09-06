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

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class SmpDescriptorExtractor {

  InstalledPluginDescriptor extractPluginDescriptor(Path file) throws IOException {
    try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(file), StandardCharsets.UTF_8)) {
      ZipEntry nextEntry;
      while ((nextEntry = zipInputStream.getNextEntry()) != null) {
        if ("META-INF/scm/plugin.xml".equals(nextEntry.getName())) {
          JAXBContext context = JAXBContext.newInstance(ScmModule.class, InstalledPluginDescriptor.class);
          return (InstalledPluginDescriptor) context.createUnmarshaller().unmarshal(zipInputStream);
        }
      }
    } catch (JAXBException e) {
      throw new IOException("failed to read descriptor file META-INF/scm/plugin.xml from plugin", e);
    }
    throw new IOException("Missing plugin descriptor META-INF/scm/plugin.xml in download package");
  }
}
