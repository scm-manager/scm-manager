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

import React, { FC } from "react";
import classNames from "classnames";
import styled from "styled-components";
import { Branch } from "@scm-manager/ui-types";
import { Select } from "./forms";
import { createA11yId } from "./createA11yId";

type Props = {
  branches: Branch[];
  onSelectBranch: (branch: Branch | undefined) => void;
  selectedBranch?: string;
  label: string;
  disabled?: boolean;
};

const ZeroflexFieldLabel = styled.div`
  flex-basis: inherit;
  flex-grow: 0;
`;

const MinWidthControl = styled.div`
  min-width: 10rem;
`;

const BranchSelector: FC<Props> = ({ branches, onSelectBranch, selectedBranch, label, disabled }) => {
  const a11yId = createA11yId("branch-select");

  if (branches) {
    return (
      <div className={classNames("field", "is-horizontal")}>
        <ZeroflexFieldLabel className={classNames("field-label", "is-normal")}>
          <label className={classNames("label", "is-size-6")} id={a11yId}>
            {label}
          </label>
        </ZeroflexFieldLabel>
        <div className="field-body">
          <div className={classNames("field", "is-narrow", "mb-0")}>
            <MinWidthControl className="control">
              <Select
                className="is-fullwidth"
                options={branches.map(b => ({ label: b.name, value: b.name }))}
                onChange={branch => onSelectBranch(branches.filter(b => b.name === branch)[0])}
                disabled={!!disabled}
                value={selectedBranch}
                addValueToOptions={true}
                ariaLabelledby={a11yId}
              />
            </MinWidthControl>
          </div>
        </div>
      </div>
    );
  } else {
    return null;
  }
};

export default BranchSelector;
