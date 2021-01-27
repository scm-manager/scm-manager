package sonia.scm.store;

import sonia.scm.repository.api.ExportFailedException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static sonia.scm.ContextEntry.ContextBuilder.noContext;

class ExportCopier {

  static void putFileContentIntoStream(Exporter exporter, Path file) {
    try (OutputStream stream = exporter.put(file.getFileName().toString(), Files.size(file))) {
      Files.copy(file, stream);
    } catch (IOException e) {
      throw new ExportFailedException(
        noContext(),
        "Could not copy file to export stream: " + file,
        e
      );
    }
  }
}
