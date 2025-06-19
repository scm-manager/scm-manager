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

package sonia.scm.importexport;

import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import sonia.scm.repository.Repository;
import sonia.scm.store.QueryableMaintenanceStore;
import sonia.scm.store.QueryableStoreFactory;
import sonia.scm.store.StoreMetaDataProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class RepositoryQueryableStoreExporter {

  private final StoreMetaDataProvider metaDataProvider;
  private final QueryableStoreFactory storeFactory;


  @Inject
  RepositoryQueryableStoreExporter(StoreMetaDataProvider metaDataProvider,
                                   QueryableStoreFactory storeFactory) {
    this.metaDataProvider = metaDataProvider;
    this.storeFactory = storeFactory;
  }

  void addQueryableStoreDataToArchive(Repository repository, File newWorkdir, TarArchiveOutputStream tempTaos) throws IOException {
    TarArchiveEntry dirEntry = new TarArchiveEntry("queryable-store-data/");
    tempTaos.putArchiveEntry(dirEntry);
    tempTaos.closeArchiveEntry();

    File dataDir = new File(newWorkdir, "queryable-store-data");
    if (!dataDir.mkdirs()) {
      throw new RuntimeException("Could not create temp directory: " + dataDir.getAbsolutePath());
    }

    exportStores(repository.getId(), dataDir);

    File[] xmlFiles = dataDir.listFiles();
    if (xmlFiles != null) {
      for (File xmlFile : xmlFiles) {
        TarArchiveEntry fileEntry = new TarArchiveEntry("queryable-store-data/" + xmlFile.getName());
        fileEntry.setSize(xmlFile.length());
        tempTaos.putArchiveEntry(fileEntry);
        Files.copy(xmlFile.toPath(), tempTaos);
        tempTaos.closeArchiveEntry();
      }
    }
    tempTaos.finish();
  }

  void exportStores(String repositoryId, File workdir) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(StoreExport.class);
      Marshaller marshaller = jaxbContext.createMarshaller();
      for (Class<?> type : metaDataProvider.getTypesWithParent(Repository.class)) {
        Collection<QueryableMaintenanceStore.RawRow> rows;
        try (QueryableMaintenanceStore<?> store = storeFactory.getForMaintenance(type, repositoryId)) {
          rows = store.readRaw();
        }
        StoreExport export = new StoreExport(type, rows);
        marshaller.marshal(export, new File(workdir, type.getName() + ".xml"));
      }
    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }

  void importStores(String repositoryId, File workdir) {
    try {
      File dataDir = new File(workdir, "queryable-store-data");
      if (!dataDir.exists() || !dataDir.isDirectory()) {
        throw new RuntimeException("Directory 'queryable-store-data' not found in workdir: " + workdir.getAbsolutePath());
      }

      JAXBContext jaxbContext = JAXBContext.newInstance(StoreExport.class);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

      for (Class<?> type : metaDataProvider.getTypesWithParent(Repository.class)) {
        File file = new File(dataDir, type.getName() + ".xml");
        if (!file.exists() || file.length() == 0) {
          continue;
        }

        StoreExport export = (StoreExport) unmarshaller.unmarshal(file);
        Collection<QueryableMaintenanceStore.RawRow> rows = export.getRows();
        if (rows == null) {
          continue;
        }

        try (QueryableMaintenanceStore<?> store = storeFactory.getForMaintenance(type, repositoryId)) {
          store.writeRaw(rows);
        }

        try {
          Files.delete(file.toPath());
          log.trace("Deleted imported file: {}", file.getAbsolutePath());
        } catch (IOException e) {
          log.error("Failed to delete imported file: {} - {}", file.getAbsolutePath(), e.getMessage());
        }
      }
    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }

  @Getter
  @XmlRootElement
  @NoArgsConstructor
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class StoreExport {
    private String type;
    private Collection<QueryableMaintenanceStore.RawRow> rows = new ArrayList<>();

    StoreExport(Class<?> type, Collection<QueryableMaintenanceStore.RawRow> rows) {
      this.type = type.getName();
      this.rows = rows != null ? rows : new ArrayList<>();
    }
  }
}
