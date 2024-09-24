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
import { File } from "@scm-manager/ui-types";
import { Button, Notification } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Props = {
  directory: File;
  isFetchingNextPage: boolean;
  fetchNextPage: () => void;
};

const TruncatedNotification: FC<Props> = ({ directory, isFetchingNextPage, fetchNextPage }) => {
  const [t] = useTranslation("repos");
  if (!directory.truncated) {
    return null;
  }

  const fileCount = (directory._embedded?.children || []).filter(file => !file.directory).length;

  return (
    <Notification type="info">
      <div className="columns is-centered">
        <div className="column">{t("sources.moreFilesAvailable", { count: fileCount })}</div>
        <Button label={t("sources.loadMore")} action={fetchNextPage} loading={isFetchingNextPage} />
      </div>
    </Notification>
  );
};

export default TruncatedNotification;
