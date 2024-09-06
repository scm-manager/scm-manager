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

import { Branch, BranchDetails, Repository } from "@scm-manager/ui-types";
import React, { FC } from "react";
import { CardList } from "@scm-manager/ui-layout";
import { ErrorNotification } from "@scm-manager/ui-components";
import { useDeleteBranch } from "@scm-manager/ui-api";
import BranchListItem from "./BranchListItem";

type Props = {
  baseUrl: string;
  repository: Repository;
  branches: Branch[];
  branchesDetails?: BranchDetails[];
};

const BranchList: FC<Props> = ({ repository, baseUrl, branches, branchesDetails }) => {
  const { isLoading, error, remove } = useDeleteBranch(repository);

  return (
    <>
      <ErrorNotification error={error} />
      <CardList>
        {branches.map((branch) => (
          <BranchListItem
            key={branch.name}
            branch={branch}
            remove={remove}
            isLoading={isLoading}
            baseUrl={baseUrl}
            repository={repository}
            branchesDetails={branchesDetails}
          />
        ))}
      </CardList>
    </>
  );
};
export default BranchList;
