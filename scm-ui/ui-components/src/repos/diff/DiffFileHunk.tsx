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

import { ExpandableHunk } from "../DiffExpander";
import React, { FC } from "react";
import LastHunkFooter from "./LastHunkFooter";
import { DiffExpandedCallback, DiffFileProps, ErrorHandler } from "./types";
import HunkFooter from "./HunkFooter";
import { Decoration, Hunk, HunkData } from "react-diff-view";
import HunkHeader from "./HunkHeader";

type Props = Pick<DiffFileProps, "file"> & {
  expandableHunk: ExpandableHunk;
  i: number;
  diffExpanded: DiffExpandedCallback;
  diffExpansionFailed: ErrorHandler;
};

const DiffFileHunk: FC<Props> = ({ expandableHunk, file, i, diffExpanded, diffExpansionFailed }) => {
  const hunk = expandableHunk.hunk;
  const items: React.ReactNode[] = [];
  if (file._links?.lines) {
    items.push(
      <HunkHeader
        key={"hunkHeader-" + hunk.content}
        expandableHunk={expandableHunk}
        diffExpanded={diffExpanded}
        diffExpansionFailed={diffExpansionFailed}
      />,
    );
  } else if (i > 0) {
    items.push(
      <Decoration key={"decoration-" + hunk.content}>
        <hr className="my-2" />
      </Decoration>,
    );
  }

  items.push(<Hunk key={"hunk-" + hunk.content} hunk={hunk as HunkData} />);
  if (file._links?.lines) {
    if (i === (file.hunks ?? []).length - 1) {
      items.push(
        <LastHunkFooter
          key={"lastHunkFooter-" + hunk.content}
          expandableHunk={expandableHunk}
          diffExpanded={diffExpanded}
          diffExpansionFailed={diffExpansionFailed}
        />,
      );
    } else {
      items.push(
        <HunkFooter
          key={"hunkFooter-" + hunk.content}
          expandableHunk={expandableHunk}
          diffExpanded={diffExpanded}
          diffExpansionFailed={diffExpansionFailed}
        />,
      );
    }
  }
  return <>{items}</>;
};

export default DiffFileHunk;
