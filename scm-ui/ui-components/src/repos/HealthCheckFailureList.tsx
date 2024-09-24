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

import { HealthCheckFailure } from "@scm-manager/ui-types";
import React, { FC } from "react";
import { useTranslation } from "react-i18next";

type Props = {
  failures?: HealthCheckFailure[];
};

const HealthCheckFailureList: FC<Props> = ({ failures }) => {
  const [t] = useTranslation("plugins");

  const translationOrDefault = (translationKey: string, defaultValue: string) => {
    const translation = t(translationKey);
    return translation === translationKey ? defaultValue : translation;
  };

  if (!failures) {
    return null;
  }

  const failureLine = (failure: HealthCheckFailure) => {
    const summary = translationOrDefault(`healthCheckFailures.${failure.id}.summary`, failure.summary);
    const description = translationOrDefault(`healthCheckFailures.${failure.id}.description`, failure.description);

    return (
      <li>
        <em>{summary}</em>
        <br />
        {description}
        <br />
        {failure.url && (
          <a href={failure.url} target="_blank" rel="noreferrer">
            {t("healthCheckFailures.detailUrl")}
          </a>
        )}
      </li>
    );
  };

  const failureComponents = failures.map(failureLine);

  return <ul>{failureComponents}</ul>;
};

export default HealthCheckFailureList;
