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

import React, { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { NotFoundError, useDiff } from "@scm-manager/ui-api";
import ErrorNotification from "../ErrorNotification";
import Loading from "../Loading";
import Notification from "../Notification";
import Diff from "./Diff";
import { DiffObjectProps } from "./DiffTypes";
import DiffStatistics from "./DiffStatistics";
import { DiffDropDown } from "../index";
import DiffFileTree from "./diff/DiffFileTree";
import { DiffContent, DiffTreeTitle, FileTreeContent, StickyFileDiffContainer } from "./diff/styledElements";
import { useLocation } from "react-router-dom";
import { getFileNameFromHash } from "./diffs";
import LayoutRadioButtons from "./LayoutRadioButtons";
import { Button, useAriaId, useLayoutWide } from "@scm-manager/ui-core";
import { useLayoutState } from "./diffLayout";

export type WhitespaceMode = "ALL" | "NONE";
export type CollapseMode = "ALL" | "MANUAL";

type Props = DiffObjectProps & {
  url: string;
  limit?: number;
  refetchOnWindowFocus?: boolean;
};

type NotificationProps = {
  fetchNextPage: () => void;
  isFetchingNextPage: boolean;
};

const DEFAULT_LIMIT = 25;
const DEFAULT_REFETCH_ON_WINDOW_FOCUS = true;

const PartialNotification: FC<NotificationProps> = ({ fetchNextPage, isFetchingNextPage }) => {
  const [t] = useTranslation("repos");

  return (
    <Notification className="mt-5" type="info">
      <div className="columns is-centered">
        <div className="column">{t("changesets.moreDiffsAvailable")}</div>
        <Button onClick={() => fetchNextPage()} isLoading={isFetchingNextPage}>
          {t("changesets.loadMore")}
        </Button>
      </div>
    </Notification>
  );
};

const LoadingDiff: FC<Props> = ({ url, limit, refetchOnWindowFocus, ...props }) => {
  if (limit == undefined) {
    limit = DEFAULT_LIMIT;
  }
  if (refetchOnWindowFocus == undefined) {
    refetchOnWindowFocus = DEFAULT_REFETCH_ON_WINDOW_FOCUS;
  }

  const [ignoreWhitespace, setIgnoreWhitespace] = useState<WhitespaceMode>("NONE");
  const [layout, setLayout] = useLayoutState();
  const [collapseMode, setCollapseMode] = useState<CollapseMode>("MANUAL");
  const [prevHash, setPrevHash] = useState("");
  const diffContentId = useAriaId();
  const location = useLocation();
  useLayoutWide();

  const { error, isLoading, data, fetchNextPage, isFetchingNextPage } = useDiff(url, {
    limit,
    refetchOnWindowFocus,
    ignoreWhitespace: ignoreWhitespace,
  });

  const [t] = useTranslation("repos");

  const setFilePath = () => {
    setPrevHash("");
    setLayout("Both");
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
      <>
        <hr />
        <div className="is-flex is-justify-content-space-between">
          <DiffStatistics data={data.statistics} />
          <DiffDropDown
            setCollapseMode={setCollapseMode}
            collapseMode={collapseMode}
            renderOnMount={true}
            ignoreWhitespacesMode={ignoreWhitespace}
            setIgnoreWhitespacesMode={(hideWhiteSpaceMode: WhitespaceMode) => {
              setIgnoreWhitespace(hideWhiteSpaceMode);
            }}
          />
        </div>
        <LayoutRadioButtons layout={layout} setLayout={setLayout} />
        <div className="is-flex columns is-multiline mb-4 mt-1 ">
          <StickyFileDiffContainer
            className={classNames(
              "column",
              "pt-0",
              layout === "Both" ? "is-one-quarter" : "is-full",
              layout !== "Diff" ? "" : " is-hidden",
            )}
          >
            <FileTreeContent className="p-0" isBorder={layout !== "Diff"}>
              <DiffTreeTitle className="title is-6 m-0 p-4">{t("changesets.diffTree.title")}</DiffTreeTitle>
              {data?.tree && (
                <DiffFileTree
                  tree={data.tree}
                  currentFile={decodeURIComponent(getFileNameFromHash(location.hash) ?? "")}
                  setCurrentFile={setFilePath}
                  gap={10}
                />
              )}
            </FileTreeContent>
          </StickyFileDiffContainer>
          <DiffContent id={diffContentId} className={layout !== "Tree" ? "column" : "is-hidden"}>
            <Diff
              defaultCollapse={collapseMode === "ALL"}
              diff={data.files}
              ignoreWhitespace={ignoreWhitespace}
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
      </>
    );
  }
};

export default LoadingDiff;
