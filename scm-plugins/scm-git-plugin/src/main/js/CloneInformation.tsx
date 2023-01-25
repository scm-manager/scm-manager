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
