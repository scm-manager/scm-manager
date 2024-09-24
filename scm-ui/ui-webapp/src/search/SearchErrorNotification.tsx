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
import { BackendError, ErrorNotification, LinkStyleButton, Notification } from "@scm-manager/ui-components";

type Props = {
  error?: Error | null;
  showHelp: () => void;
};

const ParseErrorNotification: FC<Props> = ({ showHelp }) => {
  const [t] = useTranslation("commons");
  return (
    <Notification type="warning">
      <p>{t("search.quickSearch.parseError")}</p>
      <LinkStyleButton onClick={showHelp}>{t("search.quickSearch.parseErrorHelp")}</LinkStyleButton>
    </Notification>
  );
};

const isBackendError = (error: Error | BackendError): error is BackendError => {
  return (error as BackendError).errorCode !== undefined;
};

const SearchErrorNotification: FC<Props> = ({ error, showHelp }) => {
  if (!error) {
    return null;
  }
  // 5VScek8Xp1 is the id of sonia.scm.search.QueryParseException
  if (isBackendError(error) && error.errorCode === "5VScek8Xp1") {
    return <ParseErrorNotification error={error} showHelp={showHelp} />;
  }
  return <ErrorNotification error={error} />;
};

export default SearchErrorNotification;
