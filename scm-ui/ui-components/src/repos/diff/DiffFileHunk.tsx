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
