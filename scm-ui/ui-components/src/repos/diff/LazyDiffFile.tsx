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
import React, { FC, useCallback, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { ButtonGroup } from "../../buttons";
import Icon from "../../Icon";
import { Hunk as HunkType, Link } from "@scm-manager/ui-types";
import TokenizedDiffView from "../TokenizedDiffView";
import DiffButton from "../DiffButton";
import { MenuContext, OpenInFullscreenButton } from "@scm-manager/ui-components";
import DiffExpander from "../DiffExpander";
import { Modal } from "../../modals";
import ErrorNotification from "../../ErrorNotification";
import FileTitle from "./FileTitle";
import { DiffFilePanel, FullWidthTitleHeader, MarginlessModalContent, PanelHeading } from "./styledElements";
import ChangeTag from "./ChangeTag";
import { getAnchorId, hasContent as determineHasContent, hoverFileTitle } from "./helpers";
import DiffFileHunk from "./DiffFileHunk";
import { DiffFileProps } from "./types";
import { useContentType } from "@scm-manager/ui-api";
import BinaryDiffFileContent, { canDisplayBinaryFile } from "./BinaryDiffFileContent";

type Props = DiffFileProps;

const DiffFile: FC<Props> = ({
  file: fileProp,
  isCollapsed: isCollapsedProp,
  onCollapseStateChange,
  defaultCollapse: defaultCollapseProp,
  stickyHeader,
  sideBySide: sideBySideProp,
  markConflicts = true,
  fileControlFactory,
  fileAnnotationFactory,
  onClick,
  annotationFactory,
  hunkGutterHoverIcon,
  hunkClass,
  highlightLineOnHover,
}) => {
  const [t] = useTranslation("repos");
  const [collapsed, setCollapsed] = useState(true);
  const [file, setFile] = useState(fileProp);
  const [sideBySide, setSideBySide] = useState(sideBySideProp);
  const diffExpander = useMemo(() => new DiffExpander(file), [file]);
  const [expansionError, setExpansionError] = useState<Error | null | undefined>();
  const viewType = useMemo(() => (sideBySide ? "split" : "unified"), [sideBySide]);
  const hasContent = useMemo(() => determineHasContent(file), [file]);
  const newFileLink = (file._links?.newFile as Link)?.href;
  const oldFileLink = (file._links?.oldFile as Link)?.href;
  const { data: newContentType } = useContentType(newFileLink, { enabled: !hasContent && !!newFileLink });
  const { data: oldContentType } = useContentType(oldFileLink, { enabled: !hasContent && !!oldFileLink });
  const canRenderContent = useMemo(
    () =>
      hasContent ||
      (["add", "modify", "delete"].includes(file.type) && canDisplayBinaryFile(oldContentType, newContentType)),
    [file, hasContent, newContentType, oldContentType]
  );
  const canRenderSideBySide = useMemo(() => hasContent && file.type === "modify", [file, hasContent]);

  const isCollapsed = useMemo(() => {
    if (isCollapsedProp) {
      return isCollapsedProp(fileProp);
    }
    return collapsed;
  }, [collapsed, fileProp, isCollapsedProp]);

  useEffect(() => {
    if (!isCollapsedProp) {
      let defaultCollapse = !hasContent;
      if (typeof defaultCollapseProp === "boolean") {
        defaultCollapse = defaultCollapseProp;
      } else if (typeof defaultCollapseProp === "function") {
        defaultCollapse = defaultCollapseProp(file.oldPath, file.newPath);
      }

      setCollapsed(defaultCollapse);
    }
  }, [defaultCollapseProp, file, hasContent, isCollapsedProp]);

  const toggleCollapse = useCallback(
    (event: React.MouseEvent<HTMLDivElement>) => {
      if (canRenderContent) {
        if (onCollapseStateChange) {
          onCollapseStateChange(file);
        } else {
          setCollapsed((prev) => !prev);
        }
      }
      if (stickyHeader) {
        const element = document.getElementById(event.currentTarget.id);
        // Prevent skipping diffs on collapsing long ones because of the sticky header
        // We jump to the start of the diff and afterwards go slightly up to show the diff header right under the page header
        // Only scroll if diff is not collapsed and is using the "sticky" mode
        const pageHeaderSize = 50;
        if (
          element &&
          (isCollapsedProp ? !isCollapsedProp(file) : !collapsed) &&
          element.getBoundingClientRect().top < pageHeaderSize
        ) {
          element.scrollIntoView();
          window.scrollBy(0, -pageHeaderSize);
        }
      }
    },
    [collapsed, file, canRenderContent, isCollapsedProp, onCollapseStateChange, stickyHeader]
  );

  const toggleSideBySide = useCallback((callback: () => void) => {
    setSideBySide((prev) => !prev);
    callback();
  }, []);

  const sideBySideToggle = useMemo(
    () =>
      canRenderSideBySide && (
        <MenuContext.Consumer>
          {({ setCollapsed }) => (
            <DiffButton
              icon={sideBySide ? "align-left" : "columns"}
              tooltip={t(sideBySide ? "diff.combined" : "diff.sideBySide")}
              onClick={() =>
                toggleSideBySide(() => {
                  if (sideBySide) {
                    setCollapsed(true);
                  }
                })
              }
            />
          )}
        </MenuContext.Consumer>
      ),
    [canRenderSideBySide, sideBySide, t, toggleSideBySide]
  );

  const errorModal = useMemo(
    () =>
      expansionError ? (
        <Modal
          title={t("diff.expansionFailed")}
          closeFunction={() => setExpansionError(undefined)}
          body={<ErrorNotification error={expansionError} />}
          active={true}
        />
      ) : null,
    [expansionError, t]
  );

  const innerContent = useMemo(
    () => (
      <div className="panel-block p-0">
        {fileAnnotationFactory ? fileAnnotationFactory(file) : null}
        {hasContent ? (
          <TokenizedDiffView className={viewType} viewType={viewType} file={file}>
            {(hunks: HunkType[]) =>
              hunks?.map((hunk, n) => (
                <DiffFileHunk
                  key={hunk.content}
                  file={file}
                  expandableHunk={diffExpander.getHunk(n)}
                  i={n}
                  diffExpanded={setFile}
                  diffExpansionFailed={setExpansionError}
                  annotationFactory={annotationFactory}
                  onClick={onClick}
                  hunkClass={hunkClass}
                  hunkGutterHoverIcon={hunkGutterHoverIcon}
                  highlightLineOnHover={highlightLineOnHover}
                  markConflicts={markConflicts}
                />
              ))
            }
          </TokenizedDiffView>
        ) : (
          <BinaryDiffFileContent
            newContentType={newContentType}
            oldContentType={oldContentType}
            newFileLink={newFileLink}
            oldFileLink={oldFileLink}
            sideBySide={sideBySide}
          />
        )}
      </div>
    ),
    [
      annotationFactory,
      diffExpander,
      file,
      fileAnnotationFactory,
      hasContent,
      highlightLineOnHover,
      hunkClass,
      hunkGutterHoverIcon,
      markConflicts,
      newContentType,
      newFileLink,
      oldContentType,
      oldFileLink,
      onClick,
      sideBySide,
      viewType,
    ]
  );

  const body = useMemo(
    () => (!isCollapsed && canRenderContent ? innerContent : null),
    [canRenderContent, innerContent, isCollapsed]
  );

  const openInFullscreen = useMemo(
    () =>
      canRenderContent ? (
        <OpenInFullscreenButton
          modalTitle={file.type === "delete" ? file.oldPath : file.newPath}
          modalBody={<MarginlessModalContent>{innerContent}</MarginlessModalContent>}
        />
      ) : null,
    [canRenderContent, file, innerContent]
  );

  const collapseIcon = useMemo(
    () =>
      isCollapsed ? (
        <Icon name="angle-right" color="inherit" alt={t("diff.showContent")} />
      ) : (
        <Icon name="angle-down" color="inherit" alt={t("diff.hideContent")} />
      ),
    [isCollapsed, t]
  );

  return (
    <DiffFilePanel
      className={classNames("panel", "is-size-6")}
      collapsed={!canRenderContent || isCollapsed}
      id={getAnchorId(file)}
    >
      {errorModal}
      <PanelHeading className="panel-heading" sticky={stickyHeader}>
        <div className={classNames("level", "is-flex-wrap-wrap")}>
          <FullWidthTitleHeader
            className={classNames("level-left", "is-flex", "is-clickable")}
            onClick={toggleCollapse}
            title={hoverFileTitle(file)}
            id={getAnchorId(file)}
          >
            {canRenderContent ? collapseIcon : null}
            <h4 className={classNames("has-text-weight-bold", "is-ellipsis-overflow", "is-size-6", "ml-1")}>
              <FileTitle file={file} />
            </h4>
            <ChangeTag file={file} />
          </FullWidthTitleHeader>
          <div className={classNames("level-right", "is-flex", "ml-auto")}>
            <ButtonGroup>
              {sideBySideToggle}
              {openInFullscreen}
              {fileControlFactory ? fileControlFactory(file, setCollapsed) : null}
            </ButtonGroup>
          </div>
        </div>
      </PanelHeading>
      {body}
    </DiffFilePanel>
  );
};

export default DiffFile;
