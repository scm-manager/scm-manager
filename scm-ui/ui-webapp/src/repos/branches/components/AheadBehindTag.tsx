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

type Props = {
  branch: Branch;
  details: BranchDetails;
};

type BarProps = {
  width: number;
};

const Ahead = styled.div`
  margin-left: 2px;
  padding: 3px 0 0 0;
  border-left: 1px solid gray;
`;

const Behind = styled.div`
  padding: 3px 0 0 0;
`;

const Bar = styled.span.attrs(props => ({}))<BarProps>`
  position: absolute;
  right: 0;
  height: 3px;
  width: ${props => props.width};
`;

const AheadBehindTag: FC<Props> = ({ branch, details }) => {
  const [t] = useTranslation("repos");

  if (branch.defaultBranch) {
    return null;
  }

  return (
    // <Tooltip message={t("branch.aheadBehind.tooltip")} location="top">
    <div className="columns is-flex mr-3 is-unselectable">
      <Behind className="column is-half is-flex is-justify-content-flex-end">
        <div className="has-text-grey-light is-size-7">{details.changesetsBehind}</div>
        <Bar className="has-rounded-border has-background-blue-light" width={5} />
      </Behind>
      <Ahead className="column is-half is-flex">
        <div className="has-text-grey-light is-size-7">{details.changesetsAhead}</div>
        <Bar className="has-rounded-border has-background-blue-light" width={5} />
      </Ahead>
    </div>
    // </Tooltip>
  );
};

export default AheadBehindTag;
