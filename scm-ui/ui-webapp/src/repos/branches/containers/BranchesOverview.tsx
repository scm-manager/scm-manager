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
import { useTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { CreateButton, ErrorNotification, Loading, Notification, Subtitle } from "@scm-manager/ui-components";
import { orderBranches } from "../util/orderBranches";
import BranchTable from "../components/BranchTable";
import { useBranches } from "@scm-manager/ui-api";

type Props = {
  repository: Repository;
  baseUrl: string;
};

const BranchesOverview: FC<Props> = ({ repository, baseUrl }) => {
  const { isLoading, error, data } = useBranches(repository);
  const [t] = useTranslation("repos");

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (!data || isLoading) {
    return <Loading />;
  }

  const branches = data._embedded.branches || [];

  if (branches.length === 0) {
    return <Notification type="info">{t("branches.overview.noBranches")}</Notification>;
  }

  orderBranches(branches);
  const staleBranches = branches.filter(b => b.stale);
  const activeBranches = branches.filter(b => !b.stale);

  const showCreateButton = !!data._links.create;

  return (
    <>
      <Subtitle subtitle={t("branches.overview.title")} />
      {activeBranches.length > 0 ? (
        <BranchTable repository={repository} baseUrl={baseUrl} type="active" branches={activeBranches} />
      ) : null}
      {staleBranches.length > 0 ? (
        <BranchTable repository={repository} baseUrl={baseUrl} type="stale" branches={staleBranches} />
      ) : null}
      {showCreateButton ? <CreateButton label={t("branches.overview.createButton")} link="./create" /> : null}
    </>
  );
};

export default BranchesOverview;
