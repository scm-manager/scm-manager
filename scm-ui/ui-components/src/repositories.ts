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

import { Repository } from "@scm-manager/ui-types";

// util methods for repositories

export function getProtocolLinkByType(repository: Repository, type: string) {
  let protocols = repository._links.protocol;
  if (protocols) {
    if (!Array.isArray(protocols)) {
      protocols = [protocols];
    }
    for (const proto of protocols) {
      if (proto.name === type) {
        return proto.href;
      }
    }
  }
  return null;
}
