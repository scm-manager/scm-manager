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
import { File } from "@scm-manager/ui-types";
import { Button, Notification } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Props = {
  directory: File;
  isFetchingNextPage: boolean;
  fetchNextPage: () => void;
};

const TruncatedNotification: FC<Props> = ({ directory, isFetchingNextPage, fetchNextPage }) => {
  const [t] = useTranslation("repos");
  if (!directory.truncated) {
    return null;
  }

  const fileCount = (directory._embedded?.children || []).filter((file) => !file.directory).length;

  return (
    <Notification type="info">
      <div className="columns is-centered">
        <div className="column">{t("sources.moreFilesAvailable", { count: fileCount })}</div>
        <Button label={t("sources.loadMore")} action={fetchNextPage} loading={isFetchingNextPage} />
      </div>
    </Notification>
  );
};

export default TruncatedNotification;
