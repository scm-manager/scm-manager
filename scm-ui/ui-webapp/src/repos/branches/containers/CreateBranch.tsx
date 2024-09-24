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
import { Redirect, useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import queryString from "query-string";
import { Repository } from "@scm-manager/ui-types";
import { ErrorNotification, Loading, Subtitle } from "@scm-manager/ui-components";
import BranchForm from "../components/BranchForm";
import { useBranches, useCreateBranch } from "@scm-manager/ui-api";
import { encodePart } from "../../sources/components/content/FileLink";

type Props = {
  repository: Repository;
};

const CreateBranch: FC<Props> = ({ repository }) => {
  const { isLoading: isLoadingCreate, error: errorCreate, create, branch: createdBranch } = useCreateBranch(repository);
  const { isLoading: isLoadingList, error: errorList, data: branches } = useBranches(repository);
  const location = useLocation();
  const [t] = useTranslation("repos");

  const transmittedName = (url: string): string | undefined => {
    const paramsName = queryString.parse(url).name;
    if (paramsName === null) {
      return undefined;
    }
    if (Array.isArray(paramsName)) {
      return paramsName[0];
    }
    return paramsName;
  };

  if (createdBranch) {
    return (
      <Redirect
        to={`/repo/${repository.namespace}/${repository.name}/branch/${encodeURIComponent(encodePart(createdBranch.name))}/info`}
      />
    );
  }

  if (errorList) {
    return <ErrorNotification error={errorList} />;
  }

  if (isLoadingList || !branches) {
    return <Loading />;
  }

  return (
    <>
      <Subtitle subtitle={t("branches.create.title")} />
      {errorCreate ? <ErrorNotification error={errorCreate} /> : null}
      <BranchForm
        submitForm={create}
        loading={isLoadingCreate}
        branches={branches._embedded?.branches || []}
        transmittedName={transmittedName(location.search)}
      />
    </>
  );
};

export default CreateBranch;
