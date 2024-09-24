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

package sonia.scm.work;

import com.google.common.annotations.Beta;
import jakarta.annotation.Nullable;
import sonia.scm.ModelObject;

/**
 * The {@link CentralWorkQueue} provides an api to submit and coordinate long-running or resource intensive tasks.
 *
 * The tasks are executed in parallel, but if some tasks access the same resource this can become a problem.
 * To avoid this problem a task can be enqueued with a lock e.g.:
 *
 * <pre>{@code
 *   queue.append().locks("my-resources").enqueue(MyTask.class)
 * }</pre>
 *
 * No tasks with the same lock will run in parallel. The example above locks a whole group of resources.
 * It is possible to assign the lock to a more specific resource by adding the id parameter to the lock e.g.:
 *
 * <pre>{@code
 *   queue.append().locks("my-resources", "42").enqueue(MyTask.class)
 * }</pre>
 *
 * This will ensure, that no task with a lock for the <i>my-resource 42</i> or for <i>my-resources</i> will run at the
 * same time as the task which is enqueued in the example above.
 * But this will also allow a task for <i>my-resources</i> with an id other than <i>42</i> can run in parallel.
 *
 * All tasks are executed with the permissions of the user which enqueues the task.
 * If the task should run as admin, the {@link Enqueue#runAsAdmin()} method can be used.
 *
 * Tasks which could not be finished,
 * before a restart of shutdown of the server, will be restored and executed on startup.
 * In order to achieve the persistence of tasks,
 * the enqueued task must be provided as a class or it must be serializable.
 * This could become unhandy if the task has parameters and dependencies which must be injected.
 * The injected objects should not be serialized with the task.
 * In order to avoid this, dependencies should be declared as {@code transient}
 * and injected via setters instead of the constructor parameters e.g.:
 *
 * <pre><code>
 *   public class MyTask implements Task {
 *
 *     private final Repository repository;
 *     private transient RepositoryServiceFactory repositoryServiceFactory;
 *
 *     public MyTask(Repository repository) {
 *       this.repository = repository;
 *     }
 *
 *     {@code @}Inject
 *     public void setRepositoryServiceFactory(RepositoryServiceFactory repositoryServiceFactory) {
 *       this.repositoryServiceFactory = repositoryServiceFactory;
 *     }
 *
 *     {@code @}Override
 *     public void run() {
 *       // do something with the repository and the repositoryServiceFactory
 *     }
 *
 *   }
 * </code></pre>
 *
 * The {@link CentralWorkQueue} will inject the requested members before the {@code run} method of the task is executed.
 *
 * @since 2.23.0
 */
@Beta
public interface CentralWorkQueue {

  /**
   * Append a new task to the central work queue.
   * The method will return a builder interface to configure how the task will be enqueued.
   *
   * @return builder api for enqueue a task
   */
  Enqueue append();

  /**
   * Returns the count of pending or running tasks.
   */
  int getSize();

  /**
   * Builder interface for the enqueueing of a new task.
   */
  interface Enqueue {

    /**
     * Configure a lock for the given resource type.
     * For more information on locks see the class documentation ({@link CentralWorkQueue}).
     *
     * @param resourceType resource type to lock
     * @return {@code this}
     * @see CentralWorkQueue
     */
    Enqueue locks(String resourceType);

    /**
     * Configure a lock for the resource with the given type and id.
     * Note if the id is {@code null} the whole resource type is locked.
     * For more information on locks see the class documentation ({@link CentralWorkQueue}).
     *
     * @param resourceType resource type to lock
     * @param id id of resource to lock
     * @return {@code this}
     * @see CentralWorkQueue
     */
    Enqueue locks(String resourceType, @Nullable String id);

    /**
     * Configure a lock for the resource with the given type and the id from the given {@link ModelObject}.
     * For more information on locks see the class documentation ({@link CentralWorkQueue}).
     *
     * @param resourceType resource type to lock
     * @param object which holds the id of the resource to lock
     * @return {@code this}
     * @see CentralWorkQueue
     */
    default Enqueue locks(String resourceType, ModelObject object) {
      return locks(resourceType, object.getId());
    }

    /**
     * Run the enqueued task with administrator permission.
     *
     * @return {@code this}
     */
    Enqueue runAsAdmin();

    /**
     * Enqueue the given task to {@link CentralWorkQueue}.
     * <strong>Warning: </strong> Ensure that the task is serializable.
     * If the task is not serializable an {@link NonPersistableTaskException} will be thrown.
     * For more information about the persistence of tasks see class documentation.
     *
     * @param task serializable task to enqueue
     * @see CentralWorkQueue
     */
    void enqueue(Task task);

    /**
     * Enqueue the given task to {@link CentralWorkQueue}.
     * @param task task to enqueue
     */
    void enqueue(Class<? extends Runnable> task);
  }
}
