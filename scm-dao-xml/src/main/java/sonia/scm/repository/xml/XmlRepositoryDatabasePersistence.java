/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.xml;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//~--- JDK imports ------------------------------------------------------------

@XmlRootElement(name = "repository-db")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlRepositoryDatabasePersistence {

  private Long creationTime;

  private Long lastModified;

  @XmlJavaTypeAdapter(XmlRepositoryMapAdapter.class)
  @XmlElement(name = "repositories")
  private Map<String, RepositoryPath> repositoryPathMap = new LinkedHashMap<>();

  public XmlRepositoryDatabasePersistence() {
    long c = System.currentTimeMillis();
    creationTime = c;
    lastModified = c;
  }

  static String createKey(NamespaceAndName namespaceAndName)
  {
    return namespaceAndName.getNamespace() + ":" + namespaceAndName.getName();
  }

  static String createKey(Repository repository)
  {
    return createKey(repository.getNamespaceAndName());
  }

  public void add(RepositoryPath repositoryPath)
  {
    repositoryPathMap.put(createKey(repositoryPath.getRepository()), repositoryPath);
  }

  public boolean contains(NamespaceAndName namespaceAndName)
  {
    return repositoryPathMap.containsKey(createKey(namespaceAndName));
  }

  public boolean contains(String id)
  {
    return get(id) != null;
  }

  public boolean contains(Repository repository)
  {
    return repositoryPathMap.containsKey(createKey(repository));
  }

  public void remove(Repository repository)
  {
    repositoryPathMap.remove(createKey(repository));
  }

  public Repository remove(String key)
  {
    return repositoryPathMap.remove(key).getRepository();
  }

  public Collection<Repository> getRepositories() {
    List<Repository> repositories = new ArrayList<>();
    for (RepositoryPath repositoryPath : repositoryPathMap.values()) {
      Repository repository = repositoryPath.getRepository();
      repositories.add(repository);
    }
    return repositories;
  }

  public Collection<Repository> values()
  {
    return repositoryPathMap.values().stream().map(RepositoryPath::getRepository).collect(Collectors.toList());
  }

  public Collection<RepositoryPath> getPaths() {
    return repositoryPathMap.values();
  }


  public Repository get(NamespaceAndName namespaceAndName) {
    RepositoryPath repositoryPath = repositoryPathMap.get(createKey(namespaceAndName));
    if (repositoryPath != null) {
      return repositoryPath.getRepository();
    }
    return null;
  }

  public Repository get(String id) {
    return values().stream()
      .filter(repo -> repo.getId().equals(id))
      .findFirst()
      .orElse(null);
  }

  public long getCreationTime()
  {
    return creationTime;
  }

  public long getLastModified()
  {
    return lastModified;
  }

  //~--- set methods ----------------------------------------------------------

  public void setCreationTime(long creationTime)
  {
    this.creationTime = creationTime;
  }

  public void setLastModified(long lastModified)
  {
    this.lastModified = lastModified;
  }

  public boolean containsKey(String key) {
    return repositoryPathMap.containsKey(key);
  }
}
