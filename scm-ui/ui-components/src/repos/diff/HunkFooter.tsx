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

import { ExpandableHunk } from "../DiffExpander";
import HunkExpandDivider from "../HunkExpandDivider";
import HunkExpandLink from "../HunkExpandLink";
import React, { FC, useCallback } from "react";
import { DiffExpandedCallback, ErrorHandler } from "./types";
import { useTranslation } from "react-i18next";

type Props = { expandableHunk: ExpandableHunk; diffExpanded: DiffExpandedCallback; diffExpansionFailed: ErrorHandler };

const HunkFooter: FC<Props> = ({ expandableHunk, diffExpanded, diffExpansionFailed }) => {
  const [t] = useTranslation("repos");

  const expandBottom = useCallback(
    (expandableHunk: ExpandableHunk, count: number) => () =>
      expandableHunk.expandBottom(count).then(diffExpanded).catch(diffExpansionFailed),
    [diffExpanded, diffExpansionFailed]
  );

  if (expandableHunk.maxExpandBottomRange > 0) {
    if (expandableHunk.maxExpandBottomRange <= 10) {
      return (
        <HunkExpandDivider>
          <HunkExpandLink
            icon={"fa-angle-double-down"}
            onClick={expandBottom(expandableHunk, expandableHunk.maxExpandBottomRange)}
            text={t("diff.expandComplete", { count: expandableHunk.maxExpandBottomRange })}
          />
        </HunkExpandDivider>
      );
    } else {
      return (
        <HunkExpandDivider>
          <HunkExpandLink
            icon={"fa-angle-down"}
            onClick={expandBottom(expandableHunk, 10)}
            text={t("diff.expandByLines", { count: 10 })}
          />{" "}
          <HunkExpandLink
            icon={"fa-angle-double-down"}
            onClick={expandBottom(expandableHunk, expandableHunk.maxExpandBottomRange)}
            text={t("diff.expandComplete", { count: expandableHunk.maxExpandBottomRange })}
          />
        </HunkExpandDivider>
      );
    }
  }
  // hunk footer must be defined
  return <tfoot />;
};

export default HunkFooter;
