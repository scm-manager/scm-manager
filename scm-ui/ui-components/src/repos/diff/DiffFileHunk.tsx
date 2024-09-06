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

import { Change, Hunk as HunkType } from "@scm-manager/ui-types";
import { ExpandableHunk } from "../DiffExpander";
import React, { FC, useCallback, useMemo } from "react";
import { StyledHunk } from "./styledElements";
import LastHunkFooter from "./LastHunkFooter";
import { DiffExpandedCallback, DiffFileProps, ErrorHandler } from "./types";
import HunkFooter from "./HunkFooter";
// @ts-ignore react-diff-view does not provide types
import { Decoration, getChangeKey } from "react-diff-view";
import { collectHunkAnnotations, markConflicts } from "./helpers";
import HunkHeader from "./HunkHeader";
import { ChangeEvent } from "../DiffTypes";

type Props = Pick<
  DiffFileProps,
  | "annotationFactory"
  | "onClick"
  | "file"
  | "markConflicts"
  | "hunkClass"
  | "hunkGutterHoverIcon"
  | "highlightLineOnHover"
> & {
  expandableHunk: ExpandableHunk;
  i: number;
  diffExpanded: DiffExpandedCallback;
  diffExpansionFailed: ErrorHandler;
};

const DiffFileHunk: FC<Props> = ({
  expandableHunk,
  file,
  i,
  markConflicts: shouldMarkConflicts,
  diffExpanded,
  diffExpansionFailed,
  annotationFactory,
  onClick,
  hunkClass,
  hunkGutterHoverIcon,
  highlightLineOnHover,
}) => {
  const hunk = useMemo(() => expandableHunk.hunk, [expandableHunk]);

  const handleClickEvent = useCallback(
    (change: Change, hunk: HunkType) => {
      const context = {
        changeId: getChangeKey(change),
        change,
        hunk,
        file,
      };
      if (onClick) {
        onClick(context);
      }
    },
    [file, onClick]
  );

  const gutterEvents = useMemo(
    () =>
      onClick && {
        onClick: (event: ChangeEvent) => handleClickEvent(event.change, hunk),
      },
    [handleClickEvent, hunk, onClick]
  );

  if (shouldMarkConflicts && hunk.changes) {
    markConflicts(hunk);
  }
  const items: React.ReactNode[] = [];
  if (file._links?.lines) {
    items.push(
      <HunkHeader
        key={"hunkHeader-" + hunk.content}
        expandableHunk={expandableHunk}
        diffExpanded={diffExpanded}
        diffExpansionFailed={diffExpansionFailed}
      />
    );
  } else if (i > 0) {
    items.push(
      <Decoration key={"decoration-" + hunk.content}>
        <hr className="my-2" />
      </Decoration>
    );
  }

  items.push(
    <StyledHunk
      key={"hunk-" + hunk.content}
      hunk={expandableHunk.hunk}
      widgets={collectHunkAnnotations(hunk, file, annotationFactory)}
      gutterEvents={gutterEvents}
      className={hunkClass ? hunkClass(hunk) : null}
      icon={hunkGutterHoverIcon}
      actionable={!!gutterEvents}
      highlightLineOnHover={highlightLineOnHover}
    />
  );
  if (file._links?.lines) {
    if (i === (file.hunks ?? []).length - 1) {
      items.push(
        <LastHunkFooter
          key={"lastHunkFooter-" + hunk.content}
          expandableHunk={expandableHunk}
          diffExpanded={diffExpanded}
          diffExpansionFailed={diffExpansionFailed}
        />
      );
    } else {
      items.push(
        <HunkFooter
          key={"hunkFooter-" + hunk.content}
          expandableHunk={expandableHunk}
          diffExpanded={diffExpanded}
          diffExpansionFailed={diffExpansionFailed}
        />
      );
    }
  }
  return <>{items}</>;
};

export default DiffFileHunk;
