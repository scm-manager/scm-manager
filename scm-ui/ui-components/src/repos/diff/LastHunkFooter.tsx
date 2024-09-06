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
import HunkExpandDivider from "../HunkExpandDivider";
import HunkExpandLink from "../HunkExpandLink";
import React, { FC, useCallback } from "react";
import { useTranslation } from "react-i18next";
import { DiffExpandedCallback, ErrorHandler } from "./types";

type Props = { expandableHunk: ExpandableHunk; diffExpanded: DiffExpandedCallback; diffExpansionFailed: ErrorHandler };

const LastHunkFooter: FC<Props> = ({ expandableHunk, diffExpanded, diffExpansionFailed }) => {
  const [t] = useTranslation("repos");

  const expandBottom = useCallback(
    (expandableHunk: ExpandableHunk, count: number) => () =>
      expandableHunk.expandBottom(count).then(diffExpanded).catch(diffExpansionFailed),
    [diffExpanded, diffExpansionFailed]
  );

  if (expandableHunk.maxExpandBottomRange !== 0) {
    return (
      <HunkExpandDivider>
        <HunkExpandLink
          icon={"fa-angle-down"}
          onClick={expandBottom(expandableHunk, 10)}
          text={t("diff.expandLastBottomByLines", { count: 10 })}
        />{" "}
        <HunkExpandLink
          icon={"fa-angle-double-down"}
          onClick={expandBottom(expandableHunk, expandableHunk.maxExpandBottomRange)}
          text={t("diff.expandLastBottomComplete")}
        />
      </HunkExpandDivider>
    );
  }
  // hunk header must be defined
  return <tfoot />;
};

export default LastHunkFooter;
