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

package sonia.scm.importexport;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

@XmlRootElement(name = "import")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
class RepositoryImportLog {

  private ImportType type;
  private String repositoryType;
  private String userName;
  private String userId;
  private String repositoryId;
  private String namespace;
  private String name;
  private Boolean success;
  @XmlElement(name = "entry")
  private List<Entry> entries;

  void addEntry(Entry entry) {
    if (entries == null) {
      entries = new ArrayList<>();
    }
    this.entries.add(entry);
  }

  public List<Entry> getEntries() {
    return unmodifiableList(entries);
  }

  public List<String> toLogHeader() {
    return asList(
      format("Import of repository %s/%s", namespace, name),
      format("Repository type: %s", repositoryId),
      format("Imported from: %s", type),
      format("Imported by %s (%s)", userId, userName),
      status()
    );
  }

  private String status() {
    if (success == null) {
      return "Not finished";
    } else if (success) {
      return "Finished successful";
    } else {
      return "Import failed";
    }
  }

  enum ImportType {
    FULL, URL, DUMP
  }

  @XmlRootElement(name = "entry")
  @XmlAccessorType(XmlAccessType.FIELD)
  @SuppressWarnings("java:S1068") // unused fields will be serialized to xml
  static class Entry {
    private Date time = new Date();
    private String message;

    Entry() {
    }

    Entry(String message) {
      this.message = message;
    }

    public String toLogMessage() {
      return time + " - " + message;
    }
  }
}
