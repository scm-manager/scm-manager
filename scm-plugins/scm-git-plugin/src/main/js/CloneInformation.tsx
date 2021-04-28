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
import React, { FC, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link, Repository } from "@scm-manager/ui-types";
import { apiClient } from "@scm-manager/ui-components";

type Props = {
  url: string;
  repository: Repository;
};

const CloneInformation: FC<Props> = ({ url, repository }) => {
  const [t] = useTranslation("plugins");
  const [defaultBranch, setDefaultBranch] = useState<string>("main");
  const [emptyRepository, setEmptyRepository] = useState<boolean>();

  useEffect(() => {
    if (repository) {
      apiClient
        .get((repository._links.changesets as Link).href + "?limit=1")
        .then(r => r.json())
        .then(result => {
          const empty = result._embedded.changesets.length === 0;
          setEmptyRepository(empty);
        });
    }
  }, [repository]);

  useEffect(() => {
    if (repository) {
      apiClient
        .get((repository._links.defaultBranch as Link).href)
        .then(r => r.json())
        .then(r => r.defaultBranch && setDefaultBranch(r.defaultBranch));
    }
  }, [repository]);

  return (
    <div>
      <h4>{t("scm-git-plugin.information.clone")}</h4>
      <pre>
        <code>
          git clone {url}
          <br />
          cd {repository.name}
          <br />
          git checkout -b {defaultBranch}
        </code>
      </pre>
      {emptyRepository && (
        <>
          <h4>{t("scm-git-plugin.information.create")}</h4>
          <pre>
            <code>
              git init {repository.name}
              <br />
              cd {repository.name}
              <br />
              git checkout -b {defaultBranch}
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
              git push -u origin {defaultBranch}
              <br />
            </code>
          </pre>
        </>
      )}
      <h4>{t("scm-git-plugin.information.replace")}</h4>
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
