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

import ChangesetRow from "./ChangesetRow";
import React, { FC } from "react";
import { Branch, Changeset, File, Repository } from "@scm-manager/ui-types";
import { KeyboardIterator } from "@scm-manager/ui-shortcuts";

type Props = {
  repository: Repository;
  changesets: Changeset[];
  file?: File;
  branch?: Branch;
};

const ChangesetList: FC<Props> = ({ repository, changesets, file, branch }) => {
  return (
    <KeyboardIterator>
      {changesets.map((changeset) => {
        return (
          <ChangesetRow key={changeset.id} repository={repository} changeset={changeset} file={file} branch={branch} />
        );
      })}
    </KeyboardIterator>
  );
};

export default ChangesetList;
