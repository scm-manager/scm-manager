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

package sonia.scm;

import java.util.Collections;

/**
 *
 * @version 1.6
 */
@SuppressWarnings("squid:MaximumInheritanceDepth") // exceptions have a deep inheritance depth themselves; therefore we accept this here
public class FeatureNotSupportedException extends BadRequestException {

  private static final long serialVersionUID = 256498734456613496L;

  private static final String CODE = "9SR8G0kmU1";

  public FeatureNotSupportedException(String feature)
  {
    super(Collections.emptyList(),createMessage(feature));
  }

  @Override
  public String getCode() {
    return CODE;
  }

  private static String createMessage(String feature) {
    return "feature " + feature + " is not supported by this repository";
  }
}
