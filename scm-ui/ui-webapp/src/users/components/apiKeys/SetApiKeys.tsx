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

import { Link, Me, User } from "@scm-manager/ui-types";
import React, { FC } from "react";
import { ErrorNotification, Loading, Subtitle } from "@scm-manager/ui-components";
import ApiKeyTable from "./ApiKeyTable";
import AddApiKey from "./AddApiKey";
import { useTranslation } from "react-i18next";
import { useApiKeys, useDeleteApiKey } from "@scm-manager/ui-api";
import { Link as RouterLink } from "react-router-dom";
import { useDocumentTitle } from "@scm-manager/ui-core";

type Props = {
  user: User | Me;
};

const SetApiKeys: FC<Props> = ({ user }) => {
  const [t] = useTranslation("users");
  useDocumentTitle(t("singleUser.menu.setApiKeyNavLink"), user.displayName);
  const { isLoading, data: apiKeys, error: fetchError } = useApiKeys(user);
  const { error: deletionError, remove } = useDeleteApiKey(user);
  const error = deletionError || fetchError;

  const createLink = (apiKeys?._links?.create as Link)?.href;

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (!apiKeys || isLoading) {
    return <Loading />;
  }

  return (
    <>
      <Subtitle subtitle={t("apiKey.subtitle")} />
      <p>
        {t("apiKey.text1")} <RouterLink to={"/admin/roles/"}>{t("apiKey.manageRoles")}</RouterLink>
      </p>
      <p>{t("apiKey.text2")}</p>
      <br />
      <ApiKeyTable apiKeys={apiKeys} onDelete={remove} />
      {createLink && <AddApiKey user={user} apiKeys={apiKeys} />}
    </>
  );
};

export default SetApiKeys;
