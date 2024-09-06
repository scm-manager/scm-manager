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

package sonia.scm.store;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.api.ExportFailedException;
import sonia.scm.xml.XmlStreams;
import sonia.scm.xml.XmlStreams.AutoCloseableXMLReader;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static sonia.scm.ContextEntry.ContextBuilder.noContext;
import static sonia.scm.store.ExportableBlobFileStore.BLOB_FACTORY;
import static sonia.scm.store.ExportableConfigEntryFileStore.CONFIG_ENTRY_FACTORY;
import static sonia.scm.store.ExportableConfigFileStore.CONFIG_FACTORY;
import static sonia.scm.store.ExportableDataFileStore.DATA_FACTORY;

public class FileStoreExporter implements StoreExporter {

  private static final Collection<Function<StoreType, Optional<Function<Path, ExportableStore>>>> STORE_FACTORIES =
    asList(DATA_FACTORY, BLOB_FACTORY, CONFIG_FACTORY, CONFIG_ENTRY_FACTORY);

  private static final Logger LOG = LoggerFactory.getLogger(FileStoreExporter.class);

  private final RepositoryLocationResolver locationResolver;

  @Inject
  public FileStoreExporter(RepositoryLocationResolver locationResolver) {
    this.locationResolver = locationResolver;
  }

  @Override
  public List<ExportableStore> listExportableStores(Repository repository) {
    List<ExportableStore> exportableStores = new ArrayList<>();
    Path storeDirectory = resolveStoreDirectory(repository);
    if (!Files.exists(storeDirectory)) {
      return emptyList();
    }
    try (Stream<Path> storeTypeDirectories = Files.list(storeDirectory)) {
      storeTypeDirectories.forEach(storeTypeDirectory ->
        exportStoreTypeDirectories(exportableStores, storeTypeDirectory)
      );
    } catch (IOException e) {
      throw new ExportFailedException(
        noContext(),
        "Could not list content of directory " + storeDirectory,
        e
      );
    }
    return exportableStores;
  }

  private Path resolveStoreDirectory(Repository repository) {
    return locationResolver
      .forClass(Path.class)
      .getLocation(repository.getId())
      .resolve(Store.STORE_DIRECTORY);
  }

  private void exportStoreTypeDirectories(List<ExportableStore> exportableStores, Path storeTypeDirectory) {
    try (Stream<Path> storeDirectories = Files.list(storeTypeDirectory)) {
      storeDirectories.forEach(storeDirectory ->
        getStoreFor(storeDirectory).ifPresent(exportableStores::add)
      );
    } catch (IOException e) {
      throw new ExportFailedException(
        noContext(),
        "Could not list content of directory " + storeTypeDirectory,
        e
      );
    }
  }

  private Optional<ExportableStore> getStoreFor(Path storePath) {
    return STORE_FACTORIES
      .stream()
      .map(factory -> factory.apply(getEnumForValue(storePath)))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findFirst()
      .map(f -> f.apply(storePath));
  }

  private StoreType getEnumForValue(Path storePath) {
    if (Files.isDirectory(storePath)) {
      for (StoreType type : StoreType.values()) {
        if (type.getValue().equals(storePath.getParent().getFileName().toString())) {
          return type;
        }
      }
    } else if (storePath.toString().endsWith(".xml")) {
      return determineConfigType(storePath);
    } else {
      LOG.info("ignoring unknown file '{}' in export", storePath);
    }
    return null;
  }

  private StoreType determineConfigType(Path storePath) {
    try (AutoCloseableXMLReader reader = XmlStreams.createReader(storePath)) {
      reader.nextTag();
      if (
        "configuration".equals(reader.getLocalName())
          && "config-entry".equals(reader.getAttributeValue(0))
          && "type".equals(reader.getAttributeName(0).getLocalPart())) {
        return StoreType.CONFIG_ENTRY;
      } else {
        return StoreType.CONFIG;
      }
    } catch (XMLStreamException | IOException e) {
      throw new ExportFailedException(noContext(), "Failed to read store file " + storePath, e);
    }
  }
}
