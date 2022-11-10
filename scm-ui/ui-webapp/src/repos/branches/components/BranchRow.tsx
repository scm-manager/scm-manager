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
import { Link as ReactLink } from "react-router-dom";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { Branch, BranchDetails, Link, Repository } from "@scm-manager/ui-types";
import { Button, devices, SmallLoadingSpinner } from "@scm-manager/ui-components";
import { binder } from "@scm-manager/ui-extensions";
import DefaultBranchTag from "./DefaultBranchTag";
import AheadBehindTag from "./AheadBehindTag";
import BranchCommitDateCommitter from "./BranchCommitDateCommitter";
import { useKeyboardIteratorTarget } from "@scm-manager/ui-shortcuts";

type Props = {
  repository: Repository;
  baseUrl: string;
  branch: Branch;
  onDelete: (branch: Branch) => void;
  details?: BranchDetails;
};

const AdaptTableFlow = styled.tr`
  @media screen and (max-width: ${devices.mobile.width}px) {
    td {
      display: block;
      border-left-width: 3px !important;
    }
  }
`;

const MobileFlowSpan = styled.span`
  @media screen and (min-width: ${devices.tablet.width}px) {
    margin-left: 1rem;
  }
  @media screen and (max-width: ${devices.mobile.width}px) {
    display: block;
    white-space: break-spaces;
    word-break: break-word;
  }
`;

const BranchRow: FC<Props> = ({ repository, baseUrl, branch, onDelete, details }) => {
  const to = `${baseUrl}/${encodeURIComponent(branch.name)}/info`;
  const [t] = useTranslation("repos");
  const ref = useKeyboardIteratorTarget();

  let deleteButton;
  if ((branch?._links?.delete as Link)?.href) {
    deleteButton = (
      <Button
        color="text"
        icon="trash"
        action={() => onDelete(branch)}
        title={t("branch.delete.button")}
        className="px-2"
      />
    );
  }

  const renderBranchTag = () => {
    if (branch.defaultBranch) {
      return <DefaultBranchTag defaultBranch={branch.defaultBranch} />;
    }
    if (details) {
      return <AheadBehindTag branch={branch} details={details} hiddenMobile={true} />;
    }
    return <SmallLoadingSpinner />;
  };

  const extensionProps = { repository, branch, details };
  return (
    <AdaptTableFlow>
      <td className="is-vertical-align-middle">
        <ReactLink ref={ref} to={to} title={branch.name}>
          {branch.name}
        </ReactLink>
        {branch.lastCommitDate && (
          <MobileFlowSpan className={classNames("has-text-secondary", "is-ellipsis-overflow", "is-size-7")}>
            <BranchCommitDateCommitter branch={branch} />
          </MobileFlowSpan>
        )}
      </td>
      <td className="is-vertical-align-middle has-text-centered">{renderBranchTag()}</td>
      {binder.hasExtension("repos.branches.row.details", extensionProps)
        ? binder
            .getExtensions("repos.branches.row.details", extensionProps)
            .map((e) => (
              <td className="is-vertical-align-middle has-text-centered">{React.createElement(e, extensionProps)}</td>
            ))
        : null}
      <td className="is-vertical-align-middle has-text-centered">{deleteButton}</td>
    </AdaptTableFlow>
  );
};

export default BranchRow;
