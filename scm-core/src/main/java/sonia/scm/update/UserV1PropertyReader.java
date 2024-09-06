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

package sonia.scm.update;

import java.util.Map;

public class UserV1PropertyReader implements V1PropertyReader {
  @Override
  public String getStoreName() {
    return "user-properties-v1";
  }

  @Override
  public Instance createInstance(Map<String, V1Properties> all) {
    return new MapBasedPropertyReaderInstance(all);
  }
}
