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
import { Branch } from "@scm-manager/ui-types";
import { DateFromNow } from "@scm-manager/ui-components";
import classNames from "classnames";
import { useTranslation } from "react-i18next";

const BranchCommitDateCommitter: FC<{ branch: Branch }> = ({ branch }) => {
  const [t] = useTranslation("repos");

  const committedAt = (
    <DateFromNow className={classNames("is-size-7", "has-text-secondary")} date={branch.lastCommitDate} />
  );
  if (branch.lastCommitter?.name) {
    return (
      <>
        {t("branches.table.lastCommit")} {committedAt}{" "}
        {t("branches.table.lastCommitter", { name: branch.lastCommitter?.name })}
      </>
    );
  } else {
    return committedAt;
  }
};

export default BranchCommitDateCommitter;
