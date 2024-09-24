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
import { ErrorNotification, Level, Notification, Subtitle } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { Button, ButtonVariants } from "@scm-manager/ui-core";
import { useInvalidateSearchIndices } from "@scm-manager/ui-api";

const InvalidateSearchIndex: FC = () => {
  const { invalidate, isLoading, error, isSuccess } = useInvalidateSearchIndices();
  const [t] = useTranslation("config");

  return (
    <div>
      <Subtitle subtitle={t("invalidateSearchIndex.subtitle")} />
      {isSuccess ? <Notification type="success">{t("invalidateSearchIndex.success")}</Notification> : null}
      <ErrorNotification error={error} />
      <p className="mb-4">{t("invalidateSearchIndex.description")}</p>
      <Level
        right={
          <Button variant={ButtonVariants.SIGNAL} isLoading={isLoading} onClick={invalidate}>
            {t("invalidateSearchIndex.button")}
          </Button>
        }
      />
    </div>
  );
};

export default InvalidateSearchIndex;
