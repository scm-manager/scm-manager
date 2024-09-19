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
import React, { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import { NotFoundError, useDiff } from "@scm-manager/ui-api";
import ErrorNotification from "../ErrorNotification";
import Loading from "../Loading";
import Notification from "../Notification";
import Button from "../buttons/Button";
import Diff from "./Diff";
import { DiffObjectProps } from "./DiffTypes";
import DiffStatistics from "./DiffStatistics";
import { DiffDropDown } from "../index";
import DiffFileTree from "./diff/DiffFileTree";
import { DiffContent, FileTreeContent } from "./diff/styledElements";
import { useHistory, useLocation } from "react-router-dom";
import { getFileNameFromHash } from "./diffs";

type Props = DiffObjectProps & {
  url: string;
  limit?: number;
  refetchOnWindowFocus?: boolean;
};

type NotificationProps = {
  fetchNextPage: () => void;
  isFetchingNextPage: boolean;
};

const PartialNotification: FC<NotificationProps> = ({ fetchNextPage, isFetchingNextPage }) => {
  const [t] = useTranslation("repos");

  return (
    <Notification className="mt-5" type="info">
      <div className="columns is-centered">
        <div className="column">{t("changesets.moreDiffsAvailable")}</div>
        <Button label={t("changesets.loadMore")} action={fetchNextPage} loading={isFetchingNextPage} />
      </div>
    </Notification>
  );
};

const LoadingDiff: FC<Props> = ({ url, limit, refetchOnWindowFocus, ...props }) => {
  const [ignoreWhitespace, setIgnoreWhitespace] = useState(false);
  const [collapsed, setCollapsed] = useState(false);
  const [prevHash, setPrevHash] = useState("");
  const location = useLocation();
  const history = useHistory();

  const evaluateWhiteSpace = () => {
    return ignoreWhitespace ? "ALL" : "NONE";
  };
  const { error, isLoading, data, fetchNextPage, isFetchingNextPage } = useDiff(url, {
    limit,
    refetchOnWindowFocus,
    ignoreWhitespace: evaluateWhiteSpace(),
  });
  const [t] = useTranslation("repos");

  const ignoreWhitespaces = () => {
    setIgnoreWhitespace(!ignoreWhitespace);
  };

  const collapseDiffs = () => {
    setCollapsed(!collapsed);
  };

  const setFilePath = (path: string) => {
    setPrevHash("");
    history.push(`#diff-${encodeURIComponent(path)}`);
  };

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
      <div className="is-flex has-gap-4 mb-4 mt-4 is-justify-content-space-between">
        <FileTreeContent className={"is-three-quarters"}>
          {data?.tree && (
            <DiffFileTree
              tree={data.tree}
              currentFile={decodeURIComponent(getFileNameFromHash(location.hash) ?? "")}
              setCurrentFile={setFilePath}
            />
          )}
        </FileTreeContent>
        <DiffContent>
          <div className="is-flex has-gap-4 mb-4 mt-4 is-justify-content-space-between">
            <DiffStatistics data={data.statistics} />
            <DiffDropDown collapseDiffs={collapseDiffs} ignoreWhitespaces={ignoreWhitespaces} renderOnMount={true} />
          </div>
          <Diff
            defaultCollapse={collapsed}
            diff={data.files}
            ignoreWhitespace={evaluateWhiteSpace()}
            fetchNextPage={fetchNextPage}
            isFetchingNextPage={isFetchingNextPage}
            isDataPartial={data.partial}
            prevHash={prevHash}
            setPrevHash={setPrevHash}
            {...props}
          />
          {data.partial ? (
            <PartialNotification fetchNextPage={fetchNextPage} isFetchingNextPage={isFetchingNextPage} />
          ) : null}
        </DiffContent>
      </div>
    );
  }
};

LoadingDiff.defaultProps = {
  limit: 25,
  sideBySide: false,
  refetchOnWindowFocus: true,
};

export default LoadingDiff;
