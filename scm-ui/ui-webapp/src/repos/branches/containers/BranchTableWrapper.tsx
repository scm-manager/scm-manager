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
import { Branch, HalRepresentation, Repository } from "@scm-manager/ui-types";
import { CreateButton, ErrorNotification, Notification, Subtitle } from "@scm-manager/ui-components";
import { orderBranches } from "../util/orderBranches";
import BranchTable from "../components/BranchTable";
import { useTranslation } from "react-i18next";
import { useBranchDetailsCollection } from "@scm-manager/ui-api";
import { KeyboardIterator } from "@scm-manager/ui-shortcuts";

type Props = {
  repository: Repository;
  baseUrl: string;
  data: HalRepresentation;
};

const BranchTableWrapper: FC<Props> = ({ repository, baseUrl, data }) => {
  const [t] = useTranslation("repos");
  const branches: Branch[] = (data?._embedded?.branches as Branch[]) || [];
  orderBranches(branches);
  const staleBranches = branches.filter((b) => b.stale);
  const activeBranches = branches.filter((b) => !b.stale);
  const { error, data: branchesDetails } = useBranchDetailsCollection(repository, [
    ...activeBranches,
    ...staleBranches,
  ]);

  if (branches.length === 0) {
    return <Notification type="info">{t("branches.overview.noBranches")}</Notification>;
  }

  const showCreateButton = !!data._links.create;

  return (
    <KeyboardIterator>
      <Subtitle subtitle={t("branches.overview.title")} />
      <ErrorNotification error={error} />
      {activeBranches.length > 0 ? (
        <BranchTable
          repository={repository}
          baseUrl={baseUrl}
          type="active"
          branches={activeBranches}
          branchesDetails={branchesDetails}
        />
      ) : null}
      {staleBranches.length > 0 ? (
        <BranchTable
          repository={repository}
          baseUrl={baseUrl}
          type="stale"
          branches={staleBranches}
          branchesDetails={branchesDetails}
        />
      ) : null}
      {showCreateButton ? <CreateButton label={t("branches.overview.createButton")} link="./create" /> : null}
    </KeyboardIterator>
  );
};

export default BranchTableWrapper;
