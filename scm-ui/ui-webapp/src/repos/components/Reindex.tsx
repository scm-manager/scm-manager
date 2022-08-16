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

import { Repository } from "@scm-manager/ui-types";
import React, { FC } from "react";
import { useReindexRepository } from "@scm-manager/ui-api";
import { Button, ErrorNotification, Level, Notification, Subtitle } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Props = {
  repository: Repository;
};

const Reindex: FC<Props> = ({ repository }) => {
  const [t] = useTranslation("repos");
  const { reindex, error, isLoading, isRunning } = useReindexRepository();

  return (
    <>
      <hr />
      <ErrorNotification error={error} />
      {isRunning ? <Notification type="success">{t("reindex.started")}</Notification> : null}
      <Subtitle>{t("reindex.subtitle")}</Subtitle>
      <p>{t("reindex.description")}</p>
      <Level
        right={
          <Button
            color="warning"
            icon="sync-alt"
            className="mt-4"
            action={() => reindex(repository)}
            disabled={isLoading}
            loading={isLoading}
          >
            {t("reindex.button")}
          </Button>
        }
      />
    </>
  );
};

export default Reindex;
