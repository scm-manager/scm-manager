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
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { Branch, Repository } from "@scm-manager/ui-types";
import { SmallLoadingSpinner, Subtitle, useGeneratedId } from "@scm-manager/ui-components";
import { useDocumentTitle } from "@scm-manager/ui-core";
import BranchButtonGroup from "./BranchButtonGroup";
import DefaultBranchTag from "./DefaultBranchTag";
import AheadBehindTag from "./AheadBehindTag";
import { useBranchDetails } from "@scm-manager/ui-api";
import BranchCommitDateCommitter from "./BranchCommitDateCommitter";
import CompareLink from "../../compare/CompareLink";

type Props = {
  repository: Repository;
  branch: Branch;
};

const BranchDetail: FC<Props> = ({ repository, branch }) => {
  const [t] = useTranslation("repos");
  useDocumentTitle(
    t("branch.branchWithNamespaceName", {
      branch: branch.name,
      namespace: repository.namespace,
      name: repository.name,
    })
  );
  const { data, isLoading } = useBranchDetails(repository, branch);
  const labelId = useGeneratedId();
  let aheadBehind;
  if (isLoading) {
    aheadBehind = <SmallLoadingSpinner />;
  } else if (data) {
    aheadBehind = <AheadBehindTag branch={branch} details={data} labelId={labelId} />;
  } else {
    aheadBehind = null;
  }

  const encodedBranch = encodeURIComponent(branch.name);

  return (
    <>
      <div className="media is-align-items-center">
        <div
          className={classNames(
            "media-content",
            "subtitle",
            "is-flex",
            "is-flex-wrap-wrap",
            "is-align-items-center",
            "mb-0"
          )}
        >
          <strong className="mr-1">{t("branch.name")}</strong> <Subtitle className="mb-0">{branch.name}</Subtitle>
          <DefaultBranchTag className={"ml-2"} defaultBranch={branch.defaultBranch} />
          <div className={classNames("is-ellipsis-overflow", "is-size-7", "ml-2")}>
            <BranchCommitDateCommitter branch={branch} />
          </div>
        </div>
        <CompareLink repository={repository} source={encodedBranch} target={encodedBranch} />
        <div className="media-right">
          <BranchButtonGroup repository={repository} branch={branch} />
        </div>
      </div>
      <span id={labelId} className="is-size-7 has-text-secondary">
        {t("branch.aheadBehind.label")}
      </span>
      {aheadBehind}
    </>
  );
};

export default BranchDetail;
