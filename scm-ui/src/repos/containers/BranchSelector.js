// @flow

import React from "react";
import type { Branch } from "@scm-manager/ui-types";
import DropDown from "../components/DropDown";
import { translate } from "react-i18next";
import injectSheet from "react-jss";
import { compose } from "redux";
import classNames from "classnames";

const styles = {
  zeroflex: {
    flexGrow: 0
  },
  wrapper: {
      padding: "1rem 1.5rem 0.25rem 1.5rem",
      border: "1px solid #eee",
      borderRadius: "5px 5px 0 0"
  }
};

type Props = {
  branches: Branch[], // TODO: Use generics?
  selected: (branch?: Branch) => void,
  selectedBranch: string,

  // context props
  classes: Object,
  t: string => string
};

type State = { selectedBranch?: Branch };

class BranchSelector extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {};
  }

  componentDidMount() {
    this.props.branches
      .filter(branch => branch.name === this.props.selectedBranch)
      .forEach(branch => this.setState({ selectedBranch: branch }));
  }

  render() {
    const { branches, classes, t } = this.props;

    if (branches) {
      return (
        <div className={classNames("has-background-light field", "is-horizontal", classes.wrapper)}>
          <div className={classNames("field-label", "is-normal", classes.zeroflex)}>
            <label className="label">{t("branch-selector.label")}</label>
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
        </div>
      );
    } else {
      return null;
    }
  }

  branchSelected = (branchName: string) => {
    const { branches, selected } = this.props;
    const branch = branches.find(b => b.name === branchName);

    selected(branch);
    this.setState({ selectedBranch: branch });
  };
}

export default compose(
  injectSheet(styles),
  translate("repos")
)(BranchSelector);
