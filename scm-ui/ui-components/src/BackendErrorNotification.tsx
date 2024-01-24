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
import React, { ComponentProps, FC } from "react";
import { BackendError } from "@scm-manager/ui-api";
import Notification from "./Notification";
import { useTranslation } from "react-i18next";

type Props = Omit<ComponentProps<typeof Notification>, "type" | "role"> & {
  error: BackendError;
};

/**
 * @deprecated Please import the identical module from "@scm-manager/ui-core"
 */

const BackendErrorNotification: FC<Props> = ({ error, ...props }) => {
  const [t] = useTranslation("plugins");

  const renderErrorName = () => {
    const translation = t(`errors.${error.errorCode}.displayName`);
    if (translation === error.errorCode) {
      return error.message;
    }
    return translation;
  };

  const renderErrorDescription = () => {
    const translation = t(`errors.${error.errorCode}.description`);
    if (translation === error.errorCode) {
      return "";
    }
    return translation;
  };

  const renderAdditionalMessages = () => {
    if (error.additionalMessages) {
      return (
        <>
          <hr />
          {error.additionalMessages
            .map((additionalMessage) =>
              additionalMessage.key ? t(`errors.${additionalMessage.key}.description`) : additionalMessage.message
            )
            .map((message) => (
              <p>{message}</p>
            ))}
          <hr />
        </>
      );
    }
  };

  const renderViolations = () => {
    if (error.violations) {
      return (
        <>
          <p>
            <strong>{t("errors.violations")}</strong>
          </p>
          <ul>
            {error.violations.map((violation, index) => {
              return (
                <li key={index}>
                  {violation.path && <strong>{violation.path}:</strong>} {violation.message}{" "}
                  {violation.key && t(violation.key)}
                </li>
              );
            })}
          </ul>
        </>
      );
    }
  };

  const renderMetadata = () => {
    return (
      <>
        {renderContext()}
        {renderMoreInformationLink()}
        <div className="level is-size-7">
          <div className="left" aria-hidden={true}>
            {t("errors.transactionId")} {error.transactionId}
          </div>
          <div className="right" aria-hidden={true}>
            {t("errors.errorCode")} {error.errorCode}
          </div>
        </div>
      </>
    );
  };

  const renderContext = () => {
    if (error.context) {
      return (
        <>
          <p>
            <strong>{t("errors.context")}</strong>
          </p>
          <ul>
            {error.context.map((context, index) => {
              return (
                <li key={index}>
                  <strong>{context.type}:</strong> {context.id}
                </li>
              );
            })}
          </ul>
        </>
      );
    }
  };

  const renderMoreInformationLink = () => {
    if (error.url) {
      return (
        <p>
          {t("errors.moreInfo")}{" "}
          <a href={error.url} target="_blank" rel="noreferrer" aria-label={t("error.link")}>
            {error.errorCode}
          </a>
        </p>
      );
    }
  };

  return (
    <Notification type="danger" role="alert" {...props}>
      <div className="content">
        <p className="subtitle">
          {t("error.subtitle")}
          {": "}
          {renderErrorName()}
        </p>
        <p>{renderErrorDescription()}</p>
        {renderAdditionalMessages()}
        <p>{renderViolations()}</p>
        {renderMetadata()}
      </div>
    </Notification>
  );
};

export default BackendErrorNotification;
