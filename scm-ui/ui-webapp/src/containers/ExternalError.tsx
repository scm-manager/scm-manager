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

import React from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Notification, Page } from "@scm-manager/ui-components";

type LocationState = {
  code: string;
};

const ExternalError = () => {
  const { code } = useParams<LocationState>();
  const [t] = useTranslation(["commons", "plugins"]);

  return (
    <Page title={t("app.error.title")} subtitle={t(`plugins:errors.${code}.displayName`)}>
      <Notification type="danger" role="alert">
        {t(`plugins:errors.${code}.description`)}
      </Notification>
    </Page>
  );
};

export default ExternalError;
