/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
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
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */


package sonia.scm.repository.xml;

import sonia.scm.SCMContext;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.store.StoreConstants;
import sonia.scm.store.StoreException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author Sebastian Sdorra
 */
public class XmlRepositoryMapAdapter extends XmlAdapter<XmlRepositoryList, Map<String, RepositoryPath>> {

  @Override
  public XmlRepositoryList marshal(Map<String, RepositoryPath> repositoryMap) {
    XmlRepositoryList repositoryPaths = new XmlRepositoryList(repositoryMap);
    try {
      JAXBContext context = JAXBContext.newInstance(Repository.class);
      Marshaller marshaller = context.createMarshaller();

      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      // marshall the repo_path/metadata.xml files
      for (RepositoryPath repositoryPath : repositoryPaths.getRepositoryPaths()) {
        if (repositoryPath.toBeSynchronized()) {

          File baseDirectory = SCMContext.getContext().getBaseDirectory();
          Path dir = baseDirectory.toPath().resolve(repositoryPath.getPath());

          if (!Files.isDirectory(dir)) {
            throw new InternalRepositoryException(repositoryPath.getRepository(), "repository path not found");
          }
          marshaller.marshal(repositoryPath.getRepository(), getRepositoryMetadataFile(dir.toFile()));
          repositoryPath.setToBeSynchronized(false);
        }
      }
    } catch (JAXBException ex) {
      throw new StoreException("failed to marshal repository database", ex);
    }

    return repositoryPaths;

  }

  private File getRepositoryMetadataFile(File dir) {
    return new File(dir, StoreConstants.REPOSITORY_METADATA.concat(StoreConstants.FILE_EXTENSION));
  }

  @Override
  public Map<String, RepositoryPath> unmarshal(XmlRepositoryList repositoryPaths) {
    Map<String, RepositoryPath> repositoryPathMap = new LinkedHashMap<>();
    try {
      JAXBContext context = JAXBContext.newInstance(Repository.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      for (RepositoryPath repositoryPath : repositoryPaths) {
        SCMContextProvider contextProvider = SCMContext.getContext();
        File baseDirectory = contextProvider.getBaseDirectory();
        Repository repository = (Repository) unmarshaller.unmarshal(getRepositoryMetadataFile(baseDirectory.toPath().resolve(repositoryPath.getPath()).toFile()));

        repositoryPath.setRepository(repository);
        repositoryPathMap.put(XmlRepositoryDatabase.createKey(repository), repositoryPath);
      }
    } catch (JAXBException ex) {
      throw new StoreException("failed to unmarshal object", ex);
    }
    return repositoryPathMap;
  }
}
