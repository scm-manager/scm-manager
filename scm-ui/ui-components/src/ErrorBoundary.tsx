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

import React, { useEffect } from "react";
import { useLocation, useRouteError } from "react-router-dom";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { MissingLinkError, TokenExpiredError, urls, useIndexLink } from "@scm-manager/ui-api";
import { ErrorNotification, Subtitle, Title, useDocumentTitle, Icon } from "@scm-manager/ui-core";
import ErrorPage from "./ErrorPage";

const RedirectIconContainer = styled.div`
  min-height: 256px;
`;

const RedirectPage = () => {
  const [t] = useTranslation("commons");
  useDocumentTitle(t("errorNotification.prefix"));
  return (
    <section className="section">
      <div className="container">
        <Title>{t("errorBoundary.redirect.title")}</Title>
        <Subtitle>{t("errorBoundary.redirect.subtitle")}</Subtitle>
        <RedirectIconContainer
          className={classNames(
            "is-flex",
            "is-flex-direction-column",
            "is-justify-content-center",
            "is-align-items-center",
          )}
        >
          <Icon className="fa-7x">directions</Icon>
        </RedirectIconContainer>
      </div>
    </section>
  );
};

const ErrorBoundary = () => {
  const error = useRouteError();
  const loginLink = useIndexLink("login");
  const [t] = useTranslation("commons");
  const location = useLocation();
  const isMissingLink = error instanceof MissingLinkError;
  const isTokenExpired = error instanceof TokenExpiredError;

  useEffect(() => {
    if ((isMissingLink || isTokenExpired) && loginLink) {
      window.location.assign(urls.withContextPath("/login?from=" + location.pathname));
    }
  }, [isMissingLink, loginLink, location.pathname, isTokenExpired]);

  if (isMissingLink) {
    if (loginLink) {
      return <RedirectPage />;
    }
    return (
      <ErrorPage error={error} title={t("errorNotification.prefix")} subtitle={t("errorNotification.forbidden")} />
    );
  }

  return (
    <section className="section">
      <div className="container">
        <ErrorNotification error={error as Error} />
      </div>
    </section>
  );
};

export default ErrorBoundary;
