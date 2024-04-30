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

package sonia.scm.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.Priorities;
import sonia.scm.repository.api.ModifyCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Singleton
public class RepositoryInitializer {

  private static final Logger LOG = LoggerFactory.getLogger(RepositoryInitializer.class);

  private final RepositoryServiceFactory serviceFactory;
  private final Iterable<RepositoryContentInitializer> contentInitializers;

  @Inject
  public RepositoryInitializer(RepositoryServiceFactory serviceFactory, Set<RepositoryContentInitializer> contentInitializerSet) {
    this.serviceFactory = serviceFactory;
    this.contentInitializers = Priorities.sortInstances(contentInitializerSet);
  }

  public void initialize(Repository repository, Map<String, JsonNode> contextEntries) {
    try (RepositoryService service = serviceFactory.create(repository)) {

      for (RepositoryContentInitializer initializer : contentInitializers) {
        ModifyCommandBuilder modifyCommandBuilder = service.getModifyCommand();
        InitializerContextImpl initializerContext = new InitializerContextImpl(repository, modifyCommandBuilder, contextEntries);

        initializer.initialize(initializerContext);

        if (!modifyCommandBuilder.isEmpty()) {
          modifyCommandBuilder.setCommitMessage("initialize repository");
          String revision = modifyCommandBuilder.execute();
          LOG.info("initialized repository {} as revision {}", repository, revision);
        }
      }
    } catch (IOException e) {
      throw new InternalRepositoryException(repository, "failed to initialize repository", e);
    }
  }

  private static class InitializerContextImpl implements RepositoryContentInitializer.InitializerContext {

    private final Repository repository;
    private final ModifyCommandBuilder builder;
    private final Map<String, JsonNode> contextEntries;

    private static final ObjectMapper mapper = new ObjectMapper();

    InitializerContextImpl(Repository repository, ModifyCommandBuilder builder, Map<String, JsonNode> contextEntries) {
      this.repository = repository;
      this.builder = builder;
      this.contextEntries = contextEntries;
    }

    @Override
    public Repository getRepository() {
      return repository;
    }

    @Override
    public <T> Optional<T> getEntry(String key, Class<T> type) {
      JsonNode node = contextEntries.get(key);
      if (node != null) {
        return Optional.of(mapper.convertValue(node, type));
      }
      return Optional.empty();
    }

    @Override
    public RepositoryContentInitializer.CreateFile create(String path) {
      return new CreateFileImpl(this, builder.useDefaultPath(true).createFile(path).setOverwrite(true));
    }

    @Override
    public RepositoryContentInitializer.CreateFile createWithDefaultPath(String path, boolean useDefaultPath) {
      return new CreateFileImpl(this, builder.useDefaultPath(useDefaultPath).createFile(path).setOverwrite(true));
    }
  }

  private static class CreateFileImpl implements RepositoryContentInitializer.CreateFile {

    private final RepositoryContentInitializer.InitializerContext initializerContext;
    private final ModifyCommandBuilder.WithOverwriteFlagContentLoader contentLoader;

    CreateFileImpl(RepositoryContentInitializer.InitializerContext initializerContext, ModifyCommandBuilder.WithOverwriteFlagContentLoader contentLoader) {
      this.initializerContext = initializerContext;
      this.contentLoader = contentLoader;
    }

    @Override
    public RepositoryContentInitializer.InitializerContext from(String content) throws IOException {
      return from(CharSource.wrap(content).asByteSource(StandardCharsets.UTF_8));
    }

    @Override
    public RepositoryContentInitializer.InitializerContext from(InputStream input) throws IOException {
      contentLoader.withData(input);
      return initializerContext;
    }

    @Override
    public RepositoryContentInitializer.InitializerContext from(ByteSource byteSource) throws IOException {
      contentLoader.withData(byteSource);
      return initializerContext;
    }
  }
}
