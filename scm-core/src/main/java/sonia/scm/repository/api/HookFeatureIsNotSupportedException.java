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

package sonia.scm.repository.api;

/**
 * This exception is thrown if the underlying provider of the 
 * {@link HookContext} does not support the requested {@link HookFeature}.
 *
 * @since 1.33
 */
public class HookFeatureIsNotSupportedException extends HookException
{

  private static final long serialVersionUID = -7670872902321373610L;

  private HookFeature unsupportedFeature;

  public HookFeatureIsNotSupportedException(HookFeature unsupportedFeature)
  {
    super(unsupportedFeature.toString().concat(" is not supported"));
    this.unsupportedFeature = unsupportedFeature;
  }

  public HookFeatureIsNotSupportedException(String message,
    HookFeature unsupportedFeature)
  {
    super(message);
    this.unsupportedFeature = unsupportedFeature;
  }


  public HookFeature getUnsupportedFeature()
  {
    return unsupportedFeature;
  }

}
