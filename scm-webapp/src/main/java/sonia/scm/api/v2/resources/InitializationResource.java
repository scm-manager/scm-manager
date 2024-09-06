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

package sonia.scm.api.v2.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import sonia.scm.initialization.InitializationStep;
import sonia.scm.initialization.InitializationStepResource;

import java.util.Set;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

@Path("v2/initialization/")
public class InitializationResource {

  private final Set<InitializationStepResource> steps;

  @Inject
  public InitializationResource(Set<InitializationStepResource> steps) {
    this.steps = steps;
  }

  @Path("{stepName}")
  public InitializationStepResource step(@PathParam("stepName") String stepName) {
    return steps.stream()
      .filter(step -> stepName.equals(step.name()))
      .findFirst()
      .orElseThrow(() -> notFound(entity(InitializationStep.class, stepName)));
  }
}
