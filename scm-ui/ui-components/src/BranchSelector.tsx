import React, { FC } from "react";
import classNames from "classnames";
import styled from "styled-components";
import { Branch } from "@scm-manager/ui-types";
import DropDown from "./forms/DropDown";

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

const NoBottomMarginField = styled.div`
  margin-bottom: 0 !important;
`;

const BranchSelector: FC<Props> = ({ branches, onSelectBranch, selectedBranch, label, disabled }) => {
  if (branches) {
    return (
      <div className={classNames("field", "is-horizontal")}>
        <ZeroflexFieldLabel className={classNames("field-label", "is-normal")}>
          <label className={classNames("label", "is-size-6")}>{label}</label>
        </ZeroflexFieldLabel>
        <div className="field-body">
          <NoBottomMarginField className={classNames("field", "is-narrow")}>
            <MinWidthControl className="control">
              <DropDown
                className="is-fullwidth"
                options={branches.map(b => b.name)}
                optionSelected={branch => onSelectBranch(branches.filter(b => b.name === branch)[0])}
                disabled={!!disabled}
                preselectedOption={selectedBranch}
              />
            </MinWidthControl>
          </NoBottomMarginField>
        </div>
      </div>
    );
  } else {
    return null;
  }
};

export default BranchSelector;
