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
import { DiffContent, Divider, FileTreeContent, StickyFileDiffContainer } from "./diff/styledElements";
import { useHistory, useLocation } from "react-router-dom";
import { getFileNameFromHash } from "./diffs";
import LayoutRadioButtons from "./LayoutRadioButtons";
import { useAriaId } from "@scm-manager/ui-core";
import { useLayoutState } from "./diffLayout";

export type WhitespaceMode = "ALL" | "NONE";

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
  const [ignoreWhitespace, setIgnoreWhitespace] = useState<WhitespaceMode>("NONE");
  const [layout, setLayout] = useLayoutState();
  const [collapsed, setCollapsed] = useState(false);
  const [prevHash, setPrevHash] = useState("");
  const diffContentId = useAriaId();
  const location = useLocation();
  const history = useHistory();

  const { error, isLoading, data, fetchNextPage, isFetchingNextPage } = useDiff(url, {
    limit,
    refetchOnWindowFocus,
    ignoreWhitespace: ignoreWhitespace,
  });

  const [t] = useTranslation("repos");
  const collapseDiffs = () => {
    setCollapsed(!collapsed);
  };

  const setFilePath = (path: string) => {
    setPrevHash("");
    setLayout("Both");
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
      <>
        <Divider />
        <div className="is-flex is-justify-content-space-between">
          <DiffStatistics data={data.statistics} />
          <DiffDropDown
            collapseDiffs={collapseDiffs}
            renderOnMount={true}
            ignoreWhitespacesMode={ignoreWhitespace}
            setIgnoreWhitespacesMode={(hideWhiteSpaceMode: WhitespaceMode) => {
              setIgnoreWhitespace(hideWhiteSpaceMode);
            }}
          />
        </div>
        <LayoutRadioButtons layout={layout} setLayout={setLayout} />
        <div className="is-flex mb-4 mt-1 columns is-multiline">
          <StickyFileDiffContainer
            className={
              (layout === "Both" ? "column pl-3 is-one-quarter" : "column pl-3 is-full") +
              (layout !== "Diff" ? "" : " is-hidden")
            }
          >
            <FileTreeContent className={"p-3"} isBorder={layout !== "Diff"}>
              <h3 className={"title is-6 pt-4"}>{t("changesets.diffTree.title")}</h3>
              <Divider />
              {data?.tree && (
                <DiffFileTree
                  tree={data.tree}
                  currentFile={decodeURIComponent(getFileNameFromHash(location.hash) ?? "")}
                  setCurrentFile={setFilePath}
                  gap={12}
                />
              )}
            </FileTreeContent>
          </StickyFileDiffContainer>
          <DiffContent id={diffContentId} className={layout !== "Tree" ? "column" : "is-hidden"}>
            <Diff
              defaultCollapse={collapsed}
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

LoadingDiff.defaultProps = {
  limit: 25,
  sideBySide: false,
  refetchOnWindowFocus: true,
};

export default LoadingDiff;
