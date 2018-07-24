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
import sonia.scm.xml.XmlDatabase;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

//~--- JDK imports ------------------------------------------------------------

@XmlRootElement(name = "repository-db")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlRepositoryDatabase implements XmlDatabase<Repository>
{

  public XmlRepositoryDatabase()
  {
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

  @Override
  public void add(Repository repository)
  {
    repositoryMap.put(createKey(repository), repository);
  }

  public boolean contains(NamespaceAndName namespaceAndName)
  {
    return repositoryMap.containsKey(createKey(namespaceAndName));
  }

  @Override
  public boolean contains(String id)
  {
    return get(id) != null;
  }

  public boolean contains(Repository repository)
  {
    return repositoryMap.containsKey(createKey(repository));
  }

  public void remove(Repository repository)
  {
    repositoryMap.remove(createKey(repository));
  }

  @Override
  public Repository remove(String id)
  {
    Repository r = get(id);

    remove(r);

    return r;
  }

  @Override
  public Collection<Repository> values()
  {
    return repositoryMap.values();
  }

  //~--- get methods ----------------------------------------------------------

  public Repository get(NamespaceAndName namespaceAndName)
  {
    return repositoryMap.get(createKey(namespaceAndName));
  }

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public Repository get(String id)
  {
    Repository repository = null;

    for (Repository r : values())
    {
      if (r.getId().equals(id))
      {
        repository = r;

        break;
      }
    }

    return repository;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public long getCreationTime()
  {
    return creationTime;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public long getLastModified()
  {
    return lastModified;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param creationTime
   */
  @Override
  public void setCreationTime(long creationTime)
  {
    this.creationTime = creationTime;
  }

  /**
   * Method description
   *
   *
   * @param lastModified
   */
  @Override
  public void setLastModified(long lastModified)
  {
    this.lastModified = lastModified;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Long creationTime;

  /** Field description */
  private Long lastModified;

  /** Field description */
  @XmlJavaTypeAdapter(XmlRepositoryMapAdapter.class)
  @XmlElement(name = "repositories")
  private Map<String, Repository> repositoryMap = new LinkedHashMap<>();
}
