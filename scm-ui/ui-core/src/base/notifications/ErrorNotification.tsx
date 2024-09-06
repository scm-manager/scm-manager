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

import React, { ComponentProps, FC } from "react";
import { useTranslation } from "react-i18next";
import { BackendError, ForbiddenError, UnauthorizedError, urls } from "@scm-manager/ui-api";
import Notification from "./Notification";
import BackendErrorNotification from "./BackendErrorNotification";
import { useLocation } from "react-router-dom";

type Props = ComponentProps<typeof BasicErrorMessage> & {
  error?: Error | null;
};

const LoginLink: FC = () => {
  const [t] = useTranslation("commons");
  const location = useLocation();
  const from = encodeURIComponent(location.hash ? location.pathname + location.hash : location.pathname);

  return <a href={urls.withContextPath(`/login?from=${from}`)}>{t("errorNotification.loginLink")}</a>;
};

const BasicErrorMessage: FC<Omit<ComponentProps<typeof Notification>, "type" | "role">> = ({ children, ...props }) => {
  const [t] = useTranslation("commons");

  return (
    <Notification type="danger" role="alert" {...props}>
      <strong>{t("errorNotification.prefix")}:</strong> {children}
    </Notification>
  );
};

const ErrorNotification: FC<Props> = ({ error, ...props }) => {
  const [t] = useTranslation("commons");
  if (error) {
    if (error instanceof BackendError) {
      return <BackendErrorNotification error={error} {...props} />;
    } else if (error instanceof UnauthorizedError) {
      return (
        <BasicErrorMessage {...props}>
          {t("errorNotification.timeout")} <LoginLink />
        </BasicErrorMessage>
      );
    } else if (error instanceof ForbiddenError) {
      return <BasicErrorMessage {...props}>{t("errorNotification.forbidden")}</BasicErrorMessage>;
    } else {
      return <BasicErrorMessage {...props}>{error.message}</BasicErrorMessage>;
    }
  }
  return null;
};

export default ErrorNotification;
