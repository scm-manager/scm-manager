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
import Notification from "../Notification";
import { useMe } from "@scm-manager/ui-api";

const CommitAuthor: FC = () => {
  const [t] = useTranslation("repos");
  const { data: me } = useMe();

  if (!me) {
    return null;
  }

  const mail = me.mail ? me.mail : me.fallbackMail;

  return (
    <>
      {!me.mail && <Notification type="warning">{t("commit.commitAuthor.noMail")}</Notification>}
      <div className="mb-2">
        <strong>{t("commit.commitAuthor.author")}</strong> {`${me.displayName} <${mail}>`}
      </div>
    </>
  );
};

export default CommitAuthor;
