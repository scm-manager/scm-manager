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
import ApiKeyEntry from "./ApiKeyEntry";
import { Notification } from "@scm-manager/ui-components";
import { ApiKey, ApiKeysCollection } from "@scm-manager/ui-types";
import { DeleteFunction } from "@scm-manager/ui-api";

type Props = {
  apiKeys?: ApiKeysCollection;
  onDelete: DeleteFunction<ApiKey>;
};

const ApiKeyTable: FC<Props> = ({ apiKeys, onDelete }) => {
  const [t] = useTranslation("users");

  if (apiKeys?._embedded?.keys?.length === 0) {
    return <Notification type="info">{t("apiKey.noStoredKeys")}</Notification>;
  }

  return (
    <table className="card-table table is-hoverable is-fullwidth">
      <thead>
        <tr>
          <th>{t("apiKey.displayName")}</th>
          <th>{t("apiKey.permissionRole.label")}</th>
          <th>{t("apiKey.created")}</th>
          <th />
        </tr>
      </thead>
      <tbody>
        {apiKeys?._embedded?.keys?.map((apiKey: ApiKey, index: number) => {
          return <ApiKeyEntry key={index} onDelete={onDelete} apiKey={apiKey} />;
        })}
      </tbody>
    </table>
  );
};

export default ApiKeyTable;
