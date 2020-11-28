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
import { Branch, Repository } from "@scm-manager/ui-types";
import { WithTranslation, withTranslation } from "react-i18next";
import BranchButtonGroup from "./BranchButtonGroup";
import DefaultBranchTag from "./DefaultBranchTag";
import { DateFromNow } from "@scm-manager/ui-components";
import styled from "styled-components";

type Props = WithTranslation & {
  repository: Repository;
  branch: Branch;
};

const FlexRow = styled.div`
  display: flex;
  align-items: center;
  flex-wrap: wrap;
`;

const Created = styled.div`
  margin-left: 0.5rem;
  font-size: 0.8rem;
`;

const Label = styled.strong`
  margin-right: 0.3rem;
`;

const Date = styled(DateFromNow)`
  font-size: 0.8rem;
`;

class BranchDetail extends React.Component<Props> {
  render() {
    const { repository, branch, t } = this.props;

    return (
      <div className="media">
        <FlexRow className="media-content subtitle">
          <Label>{t("branch.name")}</Label> {branch.name} <DefaultBranchTag defaultBranch={branch.defaultBranch} />
          <Created className="is-ellipsis-overflow">
            {t("tags.overview.created")} <Date date={branch.lastCommitDate} className="has-text-grey" />
          </Created>
        </FlexRow>
        <div className="media-right">
          <BranchButtonGroup repository={repository} branch={branch} />
        </div>
      </div>
    );
  }
}

export default withTranslation("repos")(BranchDetail);
