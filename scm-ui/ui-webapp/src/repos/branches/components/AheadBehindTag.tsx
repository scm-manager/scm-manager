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
