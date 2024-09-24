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
