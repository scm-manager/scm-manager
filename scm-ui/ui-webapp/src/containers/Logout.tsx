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

import React, { FC, useEffect } from "react";
import { Redirect } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useLogout } from "@scm-manager/ui-api";
import { ErrorPage, Loading } from "@scm-manager/ui-components";
import { useDocumentTitle } from "@scm-manager/ui-core";

const Logout: FC = () => {
  const { error, logout } = useLogout();
  const [t] = useTranslation("commons");
  useDocumentTitle(t("logout.title"));

  useEffect(() => {
    if (logout) {
      logout();
    }
  }, [logout]);

  if (!logout) {
    return <Redirect to={"/login"} />;
  }

  if (error) {
    return <ErrorPage title={t("logout.error.title")} subtitle={t("logout.error.subtitle")} error={error} />;
  }
  return <Loading />;
};

export default Logout;
