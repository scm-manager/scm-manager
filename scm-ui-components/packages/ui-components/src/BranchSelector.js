// @flow

import React from "react";
import type {Branch} from "@scm-manager/ui-types";
import TableHeader from "./TableHeader";
import injectSheet from "react-jss";
import classNames from "classnames";
import DropDown from "./forms/DropDown";

const styles = {
  zeroflex: {
    flexGrow: 0
  },
  minWidthOfLabel: {
    minWidth: "4.5rem"
  }
};

type Props = {
  branches: Branch[], // TODO: Use generics?
  selected: (branch?: Branch) => void,
  selectedBranch?: string,
  label: string,

  // context props
  classes: Object
};

type State = { selectedBranch?: Branch };

class BranchSelector extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {};
  }

  componentDidMount() {
    const selectedBranch = this.props.branches.find(branch => branch.name === this.props.selectedBranch);
    this.setState({ selectedBranch });
  }

  render() {
    const { branches, classes, label } = this.props;

    if (branches) {
      return (
        <TableHeader>
          <div
            className={classNames(
              "field-label",
              "is-normal",
              classes.zeroflex,
              classes.minWidthOfLabel
            )}
          >
            <label className="label">{label}</label>
          </div>
          <div className="field-body">
            <div className="field is-narrow">
              <div className="control">
                <DropDown
                  className="is-fullwidth"
                  options={branches.map(b => b.name)}
                  optionSelected={this.branchSelected}
                  preselectedOption={
                    this.state.selectedBranch
                      ? this.state.selectedBranch.name
                      : ""
                  }
                />
              </div>
            </div>
          </div>
        </TableHeader>
      );
    } else {
      return null;
    }
  }

  branchSelected = (branchName: string) => {
    const { branches, selected } = this.props;

    if (!branchName) {
      this.setState({ selectedBranch: undefined });
      selected(undefined);
      return;
    }
    const branch = branches.find(b => b.name === branchName);

    selected(branch);
    this.setState({ selectedBranch: branch });
  };
}

export default injectSheet(styles)(BranchSelector);
