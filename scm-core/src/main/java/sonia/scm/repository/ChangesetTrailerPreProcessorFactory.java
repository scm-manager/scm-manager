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

import sonia.scm.plugin.Extension;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Extension
public class ChangesetTrailerPreProcessorFactory implements ChangesetPreProcessorFactory {

  private final Set<ChangesetTrailerProvider> changesetTrailerProviderSet;

  @Inject
  public ChangesetTrailerPreProcessorFactory(Set<ChangesetTrailerProvider> changesetTrailerProviderSet) {
    this.changesetTrailerProviderSet = changesetTrailerProviderSet;
  }

  @Override
  public ChangesetPreProcessor createPreProcessor(Repository repository) {
    return changeset -> {
      Collection<Trailer> existingTrailers = changeset.getTrailers();
      List<Trailer> collectedTrailers;
      if (existingTrailers == null && existingTrailers.isEmpty()) {
        collectedTrailers = new ArrayList<>();
      } else {
        collectedTrailers = new ArrayList<>(existingTrailers);
      }
      changesetTrailerProviderSet.stream()
        .flatMap(changesetTrailers -> changesetTrailers.getTrailers(repository, changeset).stream())
        .forEach(collectedTrailers::add);
      changeset.setTrailers(collectedTrailers);
    };
  }
}
