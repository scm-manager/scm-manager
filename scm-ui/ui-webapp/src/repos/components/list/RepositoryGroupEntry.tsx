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

import React, { FC } from "react";
import { NamespaceEntries, RepositoryEntry } from "@scm-manager/ui-components";
import { RepositoryGroup } from "@scm-manager/ui-types";

type Props = {
  group: RepositoryGroup;
};

const RepositoryGroupEntry: FC<Props> = ({ group }) => {
  const entries = group.repositories.map((repository, index) => {
    return (
      <RepositoryEntry
        repository={repository}
        key={repository.name}
        expectedIndex={(group.currentPageOffset ?? 0) + index}
      />
    );
  });
  return <NamespaceEntries group={group} elements={entries} />;
};

export default RepositoryGroupEntry;
