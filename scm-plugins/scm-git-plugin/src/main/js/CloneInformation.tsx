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
import { SubSubtitle, ErrorNotification, Loading } from "@scm-manager/ui-components";
import { useChangesets, useDefaultBranch } from "@scm-manager/ui-api";

type Props = {
  url: string;
  repository: Repository;
};

const CloneInformation: FC<Props> = ({ url, repository }) => {
  const [t] = useTranslation("plugins");
  const { data: changesets, error: changesetsError, isLoading: changesetsLoading } = useChangesets(repository, {
    limit: 1
  });
  const { data: defaultBranchData, isLoading: defaultBranchLoading, error: defaultBranchError } = useDefaultBranch(
    repository
  );

  if (changesetsLoading || defaultBranchLoading) {
    return <Loading />;
  }

  const error = changesetsError || defaultBranchError;
  const branch = defaultBranchData?.defaultBranch;
  const emptyRepository = (changesets?._embedded?.changesets.length || 0) === 0;

  return (
    <div className="content">
      <ErrorNotification error={error} />
      <SubSubtitle>{t("scm-git-plugin.information.clone")}</SubSubtitle>
      <pre>
        <code>
          git clone {url}
          <br />
          cd {repository.name}
          {emptyRepository && (
            <>
              <br />
              git checkout -b {branch}
            </>
          )}
        </code>
      </pre>
      {emptyRepository && (
        <>
          <SubSubtitle>{t("scm-git-plugin.information.create")}</SubSubtitle>
          <pre>
            <code>
              git init {repository.name}
              <br />
              cd {repository.name}
              <br />
              git checkout -b {branch}
              <br />
              echo "# {repository.name}
              " &gt; README.md
              <br />
              git add README.md
              <br />
              git commit -m "Add readme"
              <br />
              git remote add origin {url}
              <br />
              git push -u origin {branch}
              <br />
            </code>
          </pre>
        </>
      )}
      <SubSubtitle>{t("scm-git-plugin.information.replace")}</SubSubtitle>
      <pre>
        <code>
          git remote add origin {url}
          <br />
        </code>
      </pre>
    </div>
  );
};

export default CloneInformation;
