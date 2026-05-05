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
import { KeyboardIterator } from "@scm-manager/ui-core";
import { Changeset, File, Repository } from "@scm-manager/ui-types";
import ChangesetRow from "./ChangesetRow";

type Props = {
  repository: Repository;
  changesets: Changeset[];
  file?: File;
  sourceName?: string;
};

const ChangesetList: FC<Props> = ({ repository, changesets, file, sourceName }) => {
  return (
    <KeyboardIterator>
      {changesets.map((changeset) => {
        return (
          <ChangesetRow
            key={changeset.id}
            repository={repository}
            changeset={changeset}
            file={file}
            sourceName={sourceName}
          />
        );
      })}
    </KeyboardIterator>
  );
};

export default ChangesetList;
