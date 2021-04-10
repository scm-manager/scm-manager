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

package sonia.scm.initialization;

import sonia.scm.EagerSingleton;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@EagerSingleton
public class DefaultInitializationFinisher implements InitializationFinisher {

  private final List<InitializationStep> steps;
  private final Provider<Set<InitializationStepResource>> resources;

  @Inject
  public DefaultInitializationFinisher(Set<InitializationStep> steps, Provider<Set<InitializationStepResource>> resources) {
    this.steps = steps.stream().sorted(comparing(InitializationStep::sequence)).collect(toList());
    this.resources = resources;
  }

  @Override
  public boolean isFullyInitialized() {
    return steps.stream().allMatch(InitializationStep::done);
  }

  @Override
  public InitializationStep missingInitialization() {
    return steps
      .stream()
      .filter(step -> !step.done()).findFirst()
      .orElseThrow(() -> new IllegalStateException("all steps initialized"));
  }

  @Override
  public InitializationStepResource getResource(String name) {
    return resources.get().stream().filter(resource -> name.equals(resource.name())).findFirst().orElseThrow(() -> new IllegalStateException("resource not found for initialization step " + name));
  }
}
