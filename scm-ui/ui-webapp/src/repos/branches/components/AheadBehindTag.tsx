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
import { Tooltip } from "@scm-manager/ui-overlays";
import { Icon } from "@scm-manager/ui-core";
import { SmallLoadingSpinner } from "@scm-manager/ui-components";

type Props = {
  branch: Branch;
  details?: BranchDetails;
};

const Count = styled.span`
  word-break: keep-all;
  width: max-content;
`;

const AheadBehindTag: FC<Props> = ({ branch, details }) => {
  const [t] = useTranslation("repos");

  if (branch.defaultBranch) {
    return null;
  }

  if (!details || typeof details.changesetsBehind !== "number" || typeof details.changesetsAhead !== "number") {
    return <SmallLoadingSpinner />;
  }

  return (
    <Tooltip
      message={t("branch.aheadBehind.tooltip", { ahead: details.changesetsAhead, behind: details.changesetsBehind })}
    >
      <>
        <span className="is-sr-only">
          {t("branch.aheadBehind.tooltip", { ahead: details.changesetsAhead, behind: details.changesetsBehind })}
        </span>
        <span className="is-inline-flex is-align-items-center" aria-hidden="true">
          <Icon className={details.changesetsAhead > 0 ? "has-text-success" : "has-text-grey-light"}>arrow-up</Icon>
          <Count className="is-size-7 pl-0">{details.changesetsAhead}</Count>
          <Icon className={details.changesetsBehind > 0 ? "has-text-warning" : "has-text-grey-light"}>arrow-down</Icon>
          <Count className="is-size-7 pr-1">{details.changesetsBehind}</Count>
        </span>
      </>
    </Tooltip>
  );
};

export default AheadBehindTag;
