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
import { Branch, Link } from "@scm-manager/ui-types";
import { DateFromNow, Icon } from "@scm-manager/ui-components";
import DefaultBranchTag from "./DefaultBranchTag";

type Props = {
  baseUrl: string;
  branch: Branch;
  onDelete: (branch: Branch) => void;
};

const BranchRow: FC<Props> = ({ baseUrl, branch, onDelete }) => {
  const to = `${baseUrl}/${encodeURIComponent(branch.name)}/info`;
  const [t] = useTranslation("repos");

  let deleteButton;
  if ((branch?._links?.delete as Link)?.href) {
    deleteButton = (
      <span
        className="icon is-small is-hovered"
        onClick={() => onDelete(branch)}
        onKeyDown={e => e.key === "Enter" && onDelete(branch)}
        tabIndex={0}
      >
        <Icon name="trash" className="fas " title={t("branch.delete.button")} />
      </span>
    );
  }

  return (
    <tr>
      <td>
        <ReactLink to={to} title={branch.name}>
          {branch.name}
          <DefaultBranchTag defaultBranch={branch.defaultBranch} />
        </ReactLink>
        {branch.lastCommitDate && (
          <span className={classNames("has-text-grey", "is-ellipsis-overflow", "is-size-7", "ml-4")}>
            {t("branches.table.lastCommit")} <DateFromNow date={branch.lastCommitDate} />
          </span>
        )}
      </td>
      <td className="is-darker has-text-centered">{deleteButton}</td>
    </tr>
  );
};

export default BranchRow;
