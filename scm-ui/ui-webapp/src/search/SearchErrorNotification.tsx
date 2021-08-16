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
import { BackendError, ErrorNotification, LinkStyleButton, Notification } from "@scm-manager/ui-components";

type Props = {
  error?: Error | null;
  showHelp: () => void;
};

const ParseErrorNotification: FC<Props> = ({ showHelp }) => {
  const [t] = useTranslation("commons");
  return (
    <Notification type="warning">
      <p>{t("search.quickSearch.parseError")}</p>
      <LinkStyleButton onClick={showHelp}>{t("search.quickSearch.parseErrorHelp")}</LinkStyleButton>
    </Notification>
  );
};

const isBackendError = (error: Error | BackendError): error is BackendError => {
  return (error as BackendError).errorCode !== undefined;
};

const SearchErrorNotification: FC<Props> = ({ error, showHelp }) => {
  if (!error) {
    return null;
  }
  // 5VScek8Xp1 is the id of sonia.scm.search.QueryParseException
  if (isBackendError(error) && error.errorCode === "5VScek8Xp1") {
    return <ParseErrorNotification error={error} showHelp={showHelp} />;
  }
  return <ErrorNotification error={error} />;
};

export default SearchErrorNotification;
