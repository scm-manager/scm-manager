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

import { Namespace } from "@scm-manager/ui-types";
import React, { FC } from "react";
import { ErrorNotification, Loading, Subtitle } from "@scm-manager/ui-core";
import { useTranslation } from "react-i18next";
import { useRepositories } from "@scm-manager/ui-api";
import { DateFromNow } from "@scm-manager/ui-components";
import { Link } from "react-router-dom";

type Props = {
  namespace: Namespace;
};

const NamespaceInformation: FC<Props> = ({ namespace }) => {
  const [t] = useTranslation("namespaces");
  const { data: repositories, error, isLoading } = useRepositories({ namespace: namespace, pageSize: 9999, page: 0 });

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (isLoading) {
    return <Loading />;
  }

  return (
    <div>
      <Subtitle subtitle={t("namespaceRoot.infoPage.subtitle")} />
      <table className="table">
        <thead>
          <tr>
            <th>{t("namespaceRoot.infoPage.repository")}</th>
            <th>{t("namespaceRoot.infoPage.type")}</th>
            <th>{t("namespaceRoot.infoPage.contact")}</th>
            <th>{t("namespaceRoot.infoPage.lastModified")}</th>
          </tr>
        </thead>
        <tbody>
          {repositories?._embedded?.repositories.map((repository) => (
            <tr key={repository.name}>
              <td>
                <Link to={`/repo/${repository.namespace}/${repository.name}`}> {repository.name}</Link>
              </td>
              <td>{repository.type}</td>
              <td>{repository.contact}</td>
              <td>
                <DateFromNow date={repository.lastModified} />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default NamespaceInformation;
