import React from "react";
import classNames from "classnames";
import styled from "styled-components";
import { Branch } from "@scm-manager/ui-types";
import DropDown from "./forms/DropDown";

type Props = {
  branches: Branch[];
  selected: (branch: Branch) => void;
  selectedBranch?: string;
  label: string;
  disabled?: boolean;
};

type State = {
  selectedBranch?: Branch;
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

export default class BranchSelector extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {};
  }

  componentDidMount() {
    const { branches } = this.props;
    if (branches) {
      const selectedBranch = branches.find(branch => branch.name === this.props.selectedBranch);
      this.setState({
        selectedBranch
      });
    }
  }

  render() {
    const { branches, label, disabled } = this.props;

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
                  optionSelected={this.branchSelected}
                  disabled={!!disabled}
                  preselectedOption={this.state.selectedBranch ? this.state.selectedBranch.name : ""}
                />
              </MinWidthControl>
            </NoBottomMarginField>
          </div>
        </div>
      );
    } else {
      return null;
    }
  }

  branchSelected = (branchName: string) => {
    const { branches, selected } = this.props;

    if (!branchName) {
      this.setState({
        selectedBranch: undefined
      });
      selected(undefined);
      return;
    }
    const branch = branches.find(b => b.name === branchName);

    selected(branch);
    this.setState({
      selectedBranch: branch
    });
  };
}
