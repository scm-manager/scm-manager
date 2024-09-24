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

import React, { FC, useCallback, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { ButtonGroup } from "../../buttons";
import { Icon } from "@scm-manager/ui-buttons";
import { Hunk as HunkType, Link } from "@scm-manager/ui-types";
import TokenizedDiffView from "../TokenizedDiffView";
import DiffButton from "../DiffButton";
import { MenuContext, OpenInFullscreenButton } from "@scm-manager/ui-components";
import DiffExpander from "../DiffExpander";
import { Modal } from "../../modals";
import { ErrorNotification } from "@scm-manager/ui-core";
import FileTitle from "./FileTitle";
import { DiffFilePanel, FullWidthTitleHeader, MarginlessModalContent, PanelHeading } from "./styledElements";
import ChangeTag from "./ChangeTag";
import { getAnchorId, hasContent as determineHasContent, hoverFileTitle } from "./helpers";
import DiffFileHunk from "./DiffFileHunk";
import { DiffFileProps } from "./types";
import { useContentType } from "@scm-manager/ui-api";
import BinaryDiffFileContent, { canDisplayBinaryFile } from "./BinaryDiffFileContent";
import styled from "styled-components";

type Props = DiffFileProps;

const Filepath = styled.h4`
  word-break: break-all;
`;

const DiffFile: FC<Props> = ({
  file: fileProp,
  isCollapsed: isCollapsedProp,
  onCollapseStateChange,
  defaultCollapse: defaultCollapseProp,
  stickyHeader,
  sideBySide: sideBySideProp,
  whitespace: whitespaceProp,
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
  const [whitespace, setWhitespace] = useState(!!whitespaceProp);
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

  const toggleWhiteSpace = useCallback(
    (callback: () => void) => {
      setWhitespace(!whitespace);
      callback();
    },
    [whitespace]
  );

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

  const whitespaceToggle = useMemo(
    () =>
      hasContent && (
        <MenuContext.Consumer>
          {({ setCollapsed }) => (
            <DiffButton
              icon={whitespace ? "laptop" : "laptop-code"}
              tooltip={t(whitespace ? "diff.hideWhitespace" : "diff.showWhitespace")}
              onClick={() =>
                toggleWhiteSpace(() => {
                  if (whitespace) {
                    setCollapsed(true);
                  }
                })
              }
            />
          )}
        </MenuContext.Consumer>
      ),
    [hasContent, t, toggleWhiteSpace, whitespace]
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
          <TokenizedDiffView className={viewType} viewType={viewType} file={file} whitespace={whitespace}>
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
      whitespace,
      viewType,
    ]
  );

  const body = useMemo(() => (!isCollapsed ? innerContent : null), [innerContent, isCollapsed]);

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
        <Icon alt={t("diff.showContent")}>angle-right</Icon>
      ) : (
        <Icon alt={t("diff.hideContent")}>angle-down</Icon>
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
            <Filepath className={classNames("has-text-weight-bold", "is-size-6", "ml-1")}>
              <FileTitle file={file} />
            </Filepath>
            <ChangeTag file={file} />
          </FullWidthTitleHeader>
          <div className={classNames("level-right", "is-flex", "ml-auto")}>
            <ButtonGroup>
              {sideBySideToggle}
              {openInFullscreen}
              {whitespaceToggle}
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
