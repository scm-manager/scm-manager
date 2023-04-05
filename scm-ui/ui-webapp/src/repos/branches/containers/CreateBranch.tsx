/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
