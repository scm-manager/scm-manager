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
import { useImportLog } from "@scm-manager/ui-api";
import { useParams } from "react-router-dom";
import { Page } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Params = {
  logId: string;
};

const ImportLog: FC = () => {
  const { logId } = useParams<Params>();
  const { isLoading, data, error } = useImportLog(logId);
  const [t] = useTranslation("commons");

  return (
    <Page title={t("importLog.title")} loading={isLoading} error={error}>
      <pre>{data ? data : null}</pre>
    </Page>
  );
};

export default ImportLog;
