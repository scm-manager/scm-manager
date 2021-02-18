/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { BackendError, ForbiddenError, UnauthorizedError, urls } from "@scm-manager/ui-api";
import Notification from "./Notification";
import BackendErrorNotification from "./BackendErrorNotification";
import { useLocation } from "react-router-dom";

type Props = {
  error?: Error | null;
};

const LoginLink: FC = () => {
  const [t] = useTranslation("commons");
  const location = useLocation();
  const from = encodeURIComponent(location.hash ? location.pathname + location.hash : location.pathname);

  return <a href={urls.withContextPath(`/login?from=${from}`)}>{t("errorNotification.loginLink")}</a>;
};

const ErrorNotification: FC<Props> = ({ error }) => {
  const [t] = useTranslation("commons");
  if (error) {
    if (error instanceof BackendError) {
      return <BackendErrorNotification error={error} />;
    } else if (error instanceof UnauthorizedError) {
      return (
        <Notification type="danger">
          <strong>{t("errorNotification.prefix")}:</strong> {t("errorNotification.timeout")} <LoginLink />
        </Notification>
      );
    } else if (error instanceof ForbiddenError) {
      return (
        <Notification type="danger">
          <strong>{t("errorNotification.prefix")}:</strong> {t("errorNotification.forbidden")}
        </Notification>
      );
    } else {
      return (
        <Notification type="danger">
          <strong>{t("errorNotification.prefix")}:</strong> {error.message}
        </Notification>
      );
    }
  }
  return null;
};

export default ErrorNotification;
