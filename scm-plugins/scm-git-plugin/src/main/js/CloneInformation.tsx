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
import { Repository } from "@scm-manager/ui-types";
import { SubSubtitle, ErrorNotification, Loading, PreformattedCodeBlock } from "@scm-manager/ui-components";
import { useChangesets, useDefaultBranch } from "@scm-manager/ui-api";

type Props = {
  url: string;
  repository: Repository;
};

const CloneInformation: FC<Props> = ({ url, repository }) => {
  const [t] = useTranslation("plugins");
  const {
    data: changesets,
    error: changesetsError,
    isLoading: changesetsLoading,
  } = useChangesets(repository, {
    limit: 1,
  });
  const {
    data: defaultBranchData,
    isLoading: defaultBranchLoading,
    error: defaultBranchError,
  } = useDefaultBranch(repository);

  if (changesetsLoading || defaultBranchLoading) {
    return <Loading />;
  }

  const error = changesetsError || defaultBranchError;
  const branch = defaultBranchData?.defaultBranch;
  const emptyRepository = (changesets?._embedded?.changesets.length || 0) === 0;

  let gitCloneCommand = `git clone ${url}\ncd ${repository.name}`;
  if (emptyRepository) {
    gitCloneCommand += `\ngit checkout -b ${branch}`;
  }
  const gitCreateCommand =
    `git init ${repository.name}\n` +
    `cd ${repository.name}\n` +
    `git checkout -b ${branch}\n` +
    `echo "# ${repository.name}" > README.md\n` +
    "git add README.md\n" +
    'git commit -m "Add readme"\n' +
    `git remote add origin ${url}\n` +
    `git push -u origin ${branch}`;
  const gitCommandCreate = `git remote add origin ${url}`;

  return (
    <div className="content">
      <ErrorNotification error={error} />
      <SubSubtitle>{t("scm-git-plugin.information.clone")}</SubSubtitle>
      <PreformattedCodeBlock>{gitCloneCommand}</PreformattedCodeBlock>
      {emptyRepository && (
        <>
          <SubSubtitle>{t("scm-git-plugin.information.create")}</SubSubtitle>
          <PreformattedCodeBlock>{gitCreateCommand}</PreformattedCodeBlock>
        </>
      )}
      <SubSubtitle>{t("scm-git-plugin.information.replace")}</SubSubtitle>
      <PreformattedCodeBlock>{gitCommandCreate}</PreformattedCodeBlock>
    </div>
  );
};

export default CloneInformation;
