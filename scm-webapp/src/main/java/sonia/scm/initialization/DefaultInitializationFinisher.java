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

package sonia.scm.initialization;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import sonia.scm.EagerSingleton;

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
    return resources.get()
      .stream()
      .filter(resource -> name.equals(resource.name()))
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("resource not found for initialization step " + name));
  }
}
