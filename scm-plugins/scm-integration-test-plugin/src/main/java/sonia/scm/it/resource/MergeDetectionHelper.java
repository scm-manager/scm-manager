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

  @Subscribe(async = false)
  public void handlePreReceiveEvent(PreReceiveRepositoryHookEvent event) {
    if (target == null || branch == null) {
      return;
    }
    preMergeDetections.add(createDto(event));
  }

  @Subscribe(async = false)
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
