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

package sonia.scm.it.resource;

import com.github.legman.Subscribe;
import de.otto.edison.hal.HalRepresentation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.spi.HookMergeDetectionProvider;

import java.util.ArrayList;
import java.util.List;

@EagerSingleton
@Extension
public class MergeDetectionHelper {

  private final List<ResultDto> preMergeDetections = new ArrayList<>();
  private final List<ResultDto> postMergeDetections = new ArrayList<>();

  private String target;
  private String branch;

  @Subscribe
  public void handlePreReceiveEvent(PreReceiveRepositoryHookEvent event) {
    if (target == null || branch == null) {
      return;
    }
    preMergeDetections.add(createDto(event));
  }

  @Subscribe
  public void handlePostReceiveEvent(PostReceiveRepositoryHookEvent event) {
    if (target == null || branch == null) {
      return;
    }
    postMergeDetections.add(createDto(event));
  }

  public ResultDto createDto(RepositoryHookEvent event) {
    HookMergeDetectionProvider mergeDetectionProvider = event.getContext().getMergeDetectionProvider();
    boolean merged = mergeDetectionProvider.branchesMerged(target, branch);
    return new ResultDto(
      event.getClass().getSimpleName(),
      event.getRepository().getNamespace(),
      event.getRepository().getName(),
      merged
    );
  }

  void initialize(String target, String branch) {
    this.target = target;
    this.branch = branch;
    preMergeDetections.clear();
    postMergeDetections.clear();
  }

  public List<ResultDto> getPreMergeDetections() {
    return preMergeDetections;
  }

  public List<ResultDto> getPostMergeDetections() {
    return postMergeDetections;
  }

  @Getter
  @AllArgsConstructor
  static class ResultDto extends HalRepresentation {
    private String type;
    private String namespace;
    private String name;
    private boolean merged;
  }
}
