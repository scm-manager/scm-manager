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
                options={branches.map((b) => ({ label: b.name, value: b.name }))}
                onChange={(branch) => onSelectBranch(branches.filter((b) => b.name === branch)[0])}
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
