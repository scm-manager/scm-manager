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
import classNames from "classnames";
import { Branch, Repository } from "@scm-manager/ui-types";
import { SmallLoadingSpinner, Subtitle } from "@scm-manager/ui-components";
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
  const { data, isLoading } = useBranchDetails(repository, branch);

  let aheadBehind;
  if (isLoading) {
    aheadBehind = <SmallLoadingSpinner />;
  } else if (data) {
    aheadBehind = <AheadBehindTag branch={branch} details={data} verbose={true} />;
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
      {aheadBehind}
    </>
  );
};

export default BranchDetail;
