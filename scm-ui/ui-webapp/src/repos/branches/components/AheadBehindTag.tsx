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

import { Branch, BranchDetails } from "@scm-manager/ui-types";
import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { Tooltip } from "@scm-manager/ui-components";
import { calculateBarLength } from "./aheadBehind";

type Props = {
  branch: Branch;
  details: BranchDetails;
  hiddenMobile?: boolean;
  verbose?: boolean;
};

type BarProps = {
  width: number;
  direction: "right" | "left";
};

const Ahead = styled.span`
  border-left: 1px solid gray;
`;

const Behind = styled.span``;

const Count = styled.span`
  word-break: keep-all;
`;

const Bar = styled.span.attrs<BarProps>(props => ({
  style: {
    width: props.width + "%",
    borderRadius: props.direction === "left" ? "25px 0 0 25px" : "0 25px 25px 0"
  }
}))<BarProps>`
  height: 3px;
  max-width: 100%;
  margin-top: -2px;
  margin-bottom: 2px;
`;

const TooltipWithDefaultCursor = styled(Tooltip)`
  cursor: default !important;
`;

const AheadBehindTag: FC<Props> = ({ branch, details, hiddenMobile, verbose }) => {
  const [t] = useTranslation("repos");

  if (
    branch.defaultBranch ||
    typeof details.changesetsBehind !== "number" ||
    typeof details.changesetsAhead !== "number"
  ) {
    return null;
  }

  const behindText = verbose
    ? t("branch.aheadBehind.behindLabel", { count: details.changesetsBehind })
    : details.changesetsBehind;
  const aheadText = verbose
    ? t("branch.aheadBehind.aheadLabel", { count: details.changesetsAhead })
    : details.changesetsAhead;

  return (
    <div className={`columns is-flex is-unselectable mt-1 ${hiddenMobile ? "is-hidden-mobile" : ""}`}>
      <TooltipWithDefaultCursor
        message={t("branch.aheadBehind.tooltip", { ahead: details.changesetsAhead, behind: details.changesetsBehind })}
        location="top"
      >
        <div className="is-flex">
          <Behind className="column is-half is-flex is-flex-direction-column is-align-items-flex-end p-0">
            <Count className="is-size-7 pr-1">{behindText}</Count>
            <Bar
              className="has-rounded-border-left has-background-secondary"
              width={calculateBarLength(details.changesetsBehind)}
              direction="left"
            />
          </Behind>
          <Ahead className="column is-half is-flex is-flex-direction-column is-align-items-flex-start p-0">
            <Count className="is-size-7 pl-1">{aheadText}</Count>
            <Bar
              className="has-rounded-border-right has-background-secondary"
              width={calculateBarLength(details.changesetsAhead)}
              direction="right"
            />
          </Ahead>
        </div>
      </TooltipWithDefaultCursor>
    </div>
  );
};

export default AheadBehindTag;
