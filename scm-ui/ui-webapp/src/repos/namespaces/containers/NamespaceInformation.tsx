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
