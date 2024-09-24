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

package sonia.scm.repository.spi;


import sonia.scm.repository.api.HookFeature;

import java.util.EnumSet;
import java.util.Set;


public class SvnHookContextProvider extends HookContextProvider
{

  private static final Set<HookFeature> SUPPORTED_FEATURES =
    EnumSet.of(HookFeature.CHANGESET_PROVIDER);

  private AbstractSvnHookChangesetProvider changesetProvider;
 
  public SvnHookContextProvider(
    AbstractSvnHookChangesetProvider changesetProvider)
  {
    this.changesetProvider = changesetProvider;
  }


  
  @Override
  public AbstractSvnHookChangesetProvider getChangesetProvider()
  {
    return changesetProvider;
  }

  
  @Override
  public Set<HookFeature> getSupportedFeatures()
  {
    return SUPPORTED_FEATURES;
  }

}
