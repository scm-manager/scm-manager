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

import React, { FC, useEffect, useState } from "react";
import { Notification } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Props = {
  isUpdated: boolean;
};

const UpdateNotification: FC<Props> = ({ isUpdated }) => {
  const [t] = useTranslation("commons");
  const [showSuccess, setShowSuccess] = useState(false);
  useEffect(() => {
    setShowSuccess(isUpdated);
  }, [isUpdated]);

  if (showSuccess) {
    return (
      <Notification type="success" onClose={() => setShowSuccess(false)}>
        {t("notifications.updateSuccessful")}
      </Notification>
    );
  }

  return null;
};

export default UpdateNotification;
