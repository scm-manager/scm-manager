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

import React, { FC, ReactNode, useEffect } from "react";
import { RouteComponentProps, useLocation, withRouter } from "react-router-dom";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { MissingLinkError, urls, useIndexLink } from "@scm-manager/ui-api";
import ErrorNotification from "./ErrorNotification";
import ErrorPage from "./ErrorPage";
import { Subtitle, Title } from "./layout";
import Icon from "./Icon";

type State = {
  error?: Error;
  errorInfo?: ErrorInfo;
};

type ExportedProps = {
  fallback?: React.ComponentType<State>;
  children: ReactNode;
};

type Props = RouteComponentProps & ExportedProps;

type ErrorInfo = {
  componentStack: string;
};

type ErrorDisplayProps = {
  fallback?: React.ComponentType<State>;
  error: Error;
  errorInfo: ErrorInfo;
};

const RedirectIconContainer = styled.div`
  min-height: 256px;
`;

const RedirectPage = () => {
  const [t] = useTranslation("commons");
  // we use an icon instead of loading spinner,
  // because a redirect is synchron and a spinner does not spin on a synchron action
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
            "is-align-items-center"
          )}
        >
          <Icon name="directions" className="fa-7x" alt="" />
        </RedirectIconContainer>
      </div>
    </section>
  );
};

const ErrorDisplay: FC<ErrorDisplayProps> = ({ error, errorInfo, fallback: FallbackComponent }) => {
  const loginLink = useIndexLink("login");
  const [t] = useTranslation("commons");
  const location = useLocation();
  const isMissingLink = error instanceof MissingLinkError;
  useEffect(() => {
    if (isMissingLink && loginLink) {
      window.location.assign(urls.withContextPath("/login?from=" + location.pathname));
    }
  }, [isMissingLink, loginLink, location.pathname]);

  if (isMissingLink) {
    if (loginLink) {
      // we can render a loading screen,
      // because the effect hook above should redirect
      return <RedirectPage />;
    } else {
      // missing link error without login link means we have no permissions
      // and we should render an error
      return (
        <ErrorPage error={error} title={t("errorNotification.prefix")} subtitle={t("errorNotification.forbidden")} />
      );
    }
  }

  if (!FallbackComponent) {
    return <ErrorNotification error={error} />;
  }

  const fallbackProps = {
    error,
    errorInfo
  };

  return <FallbackComponent {...fallbackProps} />;
};

class ErrorBoundary extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {};
  }

  componentDidUpdate(prevProps: Readonly<Props>) {
    // we must reset the error if the url has changed
    if (this.state.error && prevProps.location !== this.props.location) {
      this.setState({ error: undefined, errorInfo: undefined });
    }
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    this.setState({
      error,
      errorInfo
    });
  }

  render() {
    const { fallback } = this.props;
    const { error, errorInfo } = this.state;
    if (error && errorInfo) {
      return <ErrorDisplay error={error} errorInfo={errorInfo} fallback={fallback} />;
    }
    return this.props.children;
  }
}

export default withRouter(ErrorBoundary);
