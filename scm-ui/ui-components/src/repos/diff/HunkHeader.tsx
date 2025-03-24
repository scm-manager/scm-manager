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

import React, { FC, useCallback } from "react";
import { ExpandableHunk } from "../DiffExpander";
import HunkExpandDivider from "../HunkExpandDivider";
import HunkExpandLink from "../HunkExpandLink";
import { useTranslation } from "react-i18next";
import { DiffExpandedCallback, ErrorHandler } from "./types";

type Props = { expandableHunk: ExpandableHunk; diffExpanded: DiffExpandedCallback; diffExpansionFailed: ErrorHandler };

const HunkHeader: FC<Props> = ({ expandableHunk, diffExpanded, diffExpansionFailed }) => {
  const [t] = useTranslation("repos");

  const expandHead = useCallback(
    (expandableHunk: ExpandableHunk, count: number) => () =>
      expandableHunk.expandHead(count).then(diffExpanded).catch(diffExpansionFailed),
    [diffExpanded, diffExpansionFailed]
  );

  if (expandableHunk.maxExpandHeadRange > 0) {
    if (expandableHunk.maxExpandHeadRange <= 10) {
      return (
        <HunkExpandDivider>
          <HunkExpandLink
            icon={"angle-double-up"}
            onClick={expandHead(expandableHunk, expandableHunk.maxExpandHeadRange)}
            text={t("diff.expandComplete", { count: expandableHunk.maxExpandHeadRange })}
          />
        </HunkExpandDivider>
      );
    } else {
      return (
        <HunkExpandDivider>
          <HunkExpandLink
            icon={"angle-up"}
            onClick={expandHead(expandableHunk, 10)}
            text={t("diff.expandByLines", { count: 10 })}
          />{" "}
          <HunkExpandLink
            icon={"angle-double-up"}
            onClick={expandHead(expandableHunk, expandableHunk.maxExpandHeadRange)}
            text={t("diff.expandComplete", { count: expandableHunk.maxExpandHeadRange })}
          />
        </HunkExpandDivider>
      );
    }
  }
  // hunk header must be defined
  return <thead />;
};

export default HunkHeader;
