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
import { useTranslation, WithTranslation, withTranslation } from "react-i18next";
import { BackendError, ForbiddenError, UnauthorizedError } from "./errors";
import Notification from "./Notification";
import BackendErrorNotification from "./BackendErrorNotification";
import { useLocation } from "react-router-dom";
import { withContextPath } from "./urls";

type Props = WithTranslation & {
  error?: Error;
};

const LoginLink: FC = () => {
  const [t] = useTranslation("commons");
  const location = useLocation();
  const from = encodeURIComponent(location.pathname + location.hash);

  return <a href={withContextPath(`/login?from=${from}`)}>{t("errorNotification.loginLink")}</a>;
};

class ErrorNotification extends React.Component<Props> {
  render() {
    const { t, error } = this.props;
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
  }
}

export default withTranslation("commons")(ErrorNotification);
