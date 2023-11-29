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

package sonia.scm.search;

import com.google.common.base.Joiner;
import jakarta.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.work.CentralWorkQueue;
import sonia.scm.work.CentralWorkQueue.Enqueue;
import sonia.scm.work.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LuceneSearchEngine implements SearchEngine {

  private static final Logger LOG = LoggerFactory.getLogger(LuceneSearchEngine.class);

  private final IndexManager indexManager;
  private final SearchableTypeResolver resolver;
  private final LuceneQueryBuilderFactory queryBuilderFactory;
  private final CentralWorkQueue centralWorkQueue;

  @Inject
  public LuceneSearchEngine(IndexManager indexManager, SearchableTypeResolver resolver, LuceneQueryBuilderFactory queryBuilderFactory, CentralWorkQueue centralWorkQueue) {
    this.indexManager = indexManager;
    this.resolver = resolver;
    this.queryBuilderFactory = queryBuilderFactory;
    this.centralWorkQueue = centralWorkQueue;
  }

  @Override
  public Collection<SearchableType> getSearchableTypes() {
    Subject subject = SecurityUtils.getSubject();
    return resolver.getSearchableTypes()
      .stream()
      .filter(type -> type.getPermission().map(subject::isPermitted).orElse(true))
      .collect(Collectors.toList());
  }

  @Override
  public <T> ForType<T> forType(Class<T> type) {
    return forType(resolver.resolve(type));
  }

  @Override
  public ForType<Object> forType(String typeName) {
    return forType(resolver.resolveByName(typeName));
  }

  private <T> ForType<T> forType(LuceneSearchableType searchableType) {
    return new LuceneForType<>(searchableType);
  }

  private void enqueue(LuceneSearchableType searchableType, String index, List<String> resources, Task task) {
    Enqueue enqueuer = centralWorkQueue.append();

    String resourceName = Joiner.on('-').join(searchableType.getName(), index, "index");
    if (resources.isEmpty()) {
      enqueuer.locks(resourceName);
    } else {
      for (String resource : resources) {
        enqueuer.locks(resourceName, resource);
      }
    }

    enqueuer.runAsAdmin().enqueue(task);
  }

  @Override
  public ForIndices forIndices() {
    return new LuceneForIndices();
  }

  class LuceneForIndices implements ForIndices {

    private final List<String> resources = new ArrayList<>();
    private Predicate<IndexDetails> predicate = details -> true;

    @Override
    public ForIndices matching(Predicate<IndexDetails> predicate) {
      this.predicate = predicate;
      return this;
    }

    @Override
    public ForIndices forResource(String resource) {
      this.resources.add(resource);
      return this;
    }

    @Override
    public void batch(SerializableIndexTask<?> task) {
      exec(params -> batch(params, new LuceneSimpleIndexTask(params, task)));
    }

    @Override
    public void batch(Class<? extends IndexTask<?>> task) {
      exec(params -> batch(params, new LuceneInjectingIndexTask(params, task)));
    }

    private void exec(Consumer<IndexParams> consumer) {
      indexManager.all()
        .stream()
        .filter(predicate)
        .filter(this::isTypeAvailable)
        .map(details -> new IndexParams(details.getName(), resolver.resolve(details.getType())))
        .forEach(consumer);
    }

    private boolean isTypeAvailable(IndexDetails details) {
      if (details.getType() == null) {
        LOG.info("no type found for index with name '{}'; index will not be updated", details.getName());
        return false;
      }
      return true;
    }

    private void batch(IndexParams params, Task task) {
      LuceneSearchEngine.this.enqueue(params.getSearchableType(), params.getIndex(), resources, task);
    }
  }

  class LuceneForType<T> implements ForType<T> {

    private final LuceneSearchableType searchableType;
    private String index = "default";
    private final List<String> resources = new ArrayList<>();

    private LuceneForType(LuceneSearchableType searchableType) {
      this.searchableType = searchableType;
    }

    @Override
    public ForType<T> withIndex(String index) {
      this.index = index;
      return this;
    }

    private IndexParams params() {
      return new IndexParams(index, searchableType);
    }

    @Override
    public ForType<T> forResource(String resource) {
      resources.add(resource);
      return this;
    }

    @Override
    public void update(Class<? extends IndexTask<T>> task) {
      enqueue(new LuceneInjectingIndexTask(params(), task));
    }

    @Override
    public void update(SerializableIndexTask<T> task) {
      enqueue(new LuceneSimpleIndexTask(params(), task));
    }

    private void enqueue(Task task) {
      LuceneSearchEngine.this.enqueue(searchableType, index, resources, task);
    }

    @Override
    public QueryBuilder<T> search() {
      searchableType.getPermission().ifPresent(
        permission -> SecurityUtils.getSubject().checkPermission(permission)
      );
      return queryBuilderFactory.create(params());
    }
  }

}
