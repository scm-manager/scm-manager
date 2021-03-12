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
import { NotFoundError, useDiff } from "@scm-manager/ui-api";
import ErrorNotification from "../ErrorNotification";
import Notification from "../Notification";

import Loading from "../Loading";
import Diff from "./Diff";
import { DiffObjectProps } from "./DiffTypes";
import { useTranslation } from "react-i18next";
import Button from "../buttons/Button";
import styled from "styled-components";

type Props = DiffObjectProps & {
  url: string;
  limit?: number;
};

type NotificationProps = {
  fetchNextPage: () => void;
  isFetchingNextPage: boolean;
};

const StyledNotification = styled(Notification)`
  margin-top: 1.5rem;
`;

const PartialNotification: FC<NotificationProps> = ({ fetchNextPage, isFetchingNextPage }) => {
  const [t] = useTranslation("repos");
  return (
    <StyledNotification type="info">
      <div className="columns is-centered">
        <div className="column">{t("changesets.moreDiffsAvailable")}</div>
        <Button label={t("changesets.loadMore")} action={fetchNextPage} loading={isFetchingNextPage} />
      </div>
    </StyledNotification>
  );
};

const LoadingDiff: FC<Props> = ({ url, limit, ...props }) => {
  const { error, isLoading, data, fetchNextPage, isFetchingNextPage } = useDiff(url, { limit });
  const [t] = useTranslation("repos");

  if (error) {
    if (error instanceof NotFoundError) {
      return <Notification type="info">{t("changesets.noChangesets")}</Notification>;
    }
    return <ErrorNotification error={error} />;
  } else if (isLoading) {
    return <Loading />;
  } else if (!data?.files) {
    return null;
  } else {
    return (
      <>
        <Diff diff={data.files} {...props} />
        {data.partial ? (
          <PartialNotification fetchNextPage={fetchNextPage} isFetchingNextPage={isFetchingNextPage} />
        ) : null}
      </>
    );
  }
};

LoadingDiff.defaultProps = {
  limit: 25,
  sideBySide: false
};

export default LoadingDiff;
