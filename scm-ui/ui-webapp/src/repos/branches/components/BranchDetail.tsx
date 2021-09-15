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
import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import classNames from "classnames";
import { Branch, Repository } from "@scm-manager/ui-types";
import { DateFromNow } from "@scm-manager/ui-components";
import BranchButtonGroup from "./BranchButtonGroup";
import DefaultBranchTag from "./DefaultBranchTag";

type Props = WithTranslation & {
  repository: Repository;
  branch: Branch;
};

class BranchDetail extends React.Component<Props> {
  render() {
    const { repository, branch, t } = this.props;

    return (
      <div className="media">
        <div
          className={classNames("media-content", "subtitle", "is-flex", "is-align-items-center", "is-flex-wrap-wrap")}
        >
          <strong className="mr-1">{t("branch.name")}</strong> {branch.name}{" "}
          <DefaultBranchTag defaultBranch={branch.defaultBranch} />
          <div className={classNames("is-ellipsis-overflow", "is-size-7", "ml-2")}>
            {t("branches.overview.lastCommit")}{" "}
            <DateFromNow className={classNames("is-size-7", "has-text-grey")} date={branch.lastCommitDate} />
          </div>
        </div>
        <div className="media-right">
          <BranchButtonGroup repository={repository} branch={branch} />
        </div>
      </div>
    );
  }
}

export default withTranslation("repos")(BranchDetail);
