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
import { BackendError, ForbiddenError } from "@scm-manager/ui-api";
import { useDocumentTitle } from "@scm-manager/ui-core";
import ErrorNotification from "./ErrorNotification";

type Props = {
  error: Error;
  title: string;
  subtitle: string;
};

const ErrorPage: FC<Props> = ({ error, title, subtitle }) => {
  const [t] = useTranslation("commons");
  useDocumentTitle(t("errorNotification.prefix"));

  const renderSubtitle = () => {
    if (error instanceof BackendError || error instanceof ForbiddenError) {
      return null;
    }
    return <p className="subtitle">{subtitle}</p>;
  };

  return (
    <section className="section">
      <div className="box column is-4 is-offset-4 container">
        <h1 className="title">{title}</h1>
        {renderSubtitle()}
        <ErrorNotification error={error} />
      </div>
    </section>
  );
};

export default ErrorPage;
