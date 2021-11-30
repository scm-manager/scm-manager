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

type Props = {
  branch: Branch;
  details: BranchDetails;
};

type BarProps = {
  width: number;
  direction: "right" | "left";
};

const Ahead = styled.div`
  border-left: 1px solid gray;
`;

const Behind = styled.div``;

const Count = styled.div`
  word-break: keep-all;
`;

const Bar = styled.span.attrs(props => ({}))<BarProps>`
  height: 3px;
  max-width: 100%;
  margin-top: -2px;
  margin-bottom: 2px;
  width: ${props => props.width}%;
  border-radius: ${props => (props.direction === "left" ? "25px 0 0 25px" : "0 25px 25px 0")};
`;

const AheadBehindTag: FC<Props> = ({ branch, details }) => {
  const [t] = useTranslation("repos");

  if (
    branch.defaultBranch ||
    typeof details.changesetsBehind !== "number" ||
    typeof details.changesetsAhead !== "number"
  ) {
    return null;
  }

  const calculateBarLength = (part: number) => {
    if (part <= 10) {
      return part + 5;
    } else if (part <= 50) {
      return (part - 10) / 5 + 15;
    } else if (part <= 500) {
      return (part - 50) / 10 + 23;
    } else if (part <= 3700) {
      return (part - 500) / 100 + 68;
    } else {
      return 100;
    }
  };

  return (
    <Tooltip
      message={t("branch.aheadBehind.tooltip", { ahead: details.changesetsAhead, behind: details.changesetsBehind })}
      location="top"
    >
      <div className="columns is-flex is-unselectable is-hidden-mobile">
        <Behind className="column is-half is-flex is-flex-direction-column is-align-items-flex-end p-0">
          <Count className="is-size-7 pr-1">{details.changesetsBehind}</Count>
          <Bar
            className="has-rounded-border-left has-background-grey"
            width={calculateBarLength(details.changesetsBehind)}
            direction="left"
          />
        </Behind>
        <Ahead className="column is-half is-flex is-flex-direction-column is-align-items-flex-start p-0">
          <Count className="is-size-7 pl-1">{details.changesetsAhead}</Count>
          <Bar
            className="has-rounded-border-right has-background-grey"
            width={calculateBarLength(details.changesetsAhead)}
            direction="right"
          />
        </Ahead>
      </div>
    </Tooltip>
  );
};

export default AheadBehindTag;
