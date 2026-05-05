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
import BranchView from "../components/BranchView";
import { Routes, Route, Navigate, useParams, useLocation } from "react-router-dom";
import { ErrorNotification, Loading, NotFoundError } from "@scm-manager/ui-components";
import queryString from "query-string";
import { useBranch } from "@scm-manager/ui-api";
import { useRepositoryFromParams } from "@scm-manager/ui-core";

type Params = {
  branch: string;
};

const BranchRoot: FC = () => {
  const repository = useRepositoryFromParams();
  const { branch: branchName } = useParams<Params>();
  const { isLoading, error, data: branch } = useBranch(repository, branchName!);
  const location = useLocation();

  if (isLoading) {
    return <Loading />;
  }

  if (error) {
    if (error instanceof NotFoundError && queryString.parse(location.search).create === "true" && branchName) {
      return (
        <Navigate
          to={`/repo/${repository.namespace}/${repository.name}/branches/create?name=${encodeURIComponent(branchName)}`}
        />
      );
    }

    return <ErrorNotification error={error} />;
  }

  if (!branch) {
    return null;
  }

  return (
    <Routes>
      <Route index element={<Navigate to="info" replace />} />
      <Route path="info" element={<BranchView repository={repository} branch={branch} />} />
    </Routes>
  );
};

export default BranchRoot;
