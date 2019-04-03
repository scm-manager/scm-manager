// @flow
import React from "react";
import { translate } from "react-i18next";
import type { Repository, Branch } from "@scm-manager/ui-types";
import {
  Select,
  InputField,
  SubmitButton,
  validation as validator
} from "@scm-manager/ui-components";
import { orderBranches } from "../util/orderBranches";

type Props = {
  submitForm: Branch => void,
  repository: Repository,
  branches: Branch[],
  loading?: boolean,
  t: string => string
};

type State = {
  source?: string,
  name?: string,
  nameValidationError: boolean
};

class BranchForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      nameValidationError: false
    };
  }

  isFalsy(value) {
    return !value;
  }

  isValid = () => {
    const { source, name } = this.state;
    return !(
      this.state.nameValidationError ||
      this.isFalsy(source) ||
      this.isFalsy(name)
    );
  };

  submit = (event: Event) => {
    event.preventDefault();
    if (this.isValid()) {
      this.props.submitForm(this.state.branch);
    }
  };

  render() {
    const { t, branches, loading } = this.props;
    const { name } = this.state;
    orderBranches(branches);
    const options = branches.map(branch => ({
      label: branch.name,
      value: branch.name
    }));

    return (
      <>
        <form onSubmit={this.submit}>
          <div className="columns">
            <div className="column">
              <Select
                name="source"
                label={t("branches.create.source")}
                options={options}
                onChange={this.handleSourceChange}
                loading={loading}
              />
              <InputField
                name="name"
                label={t("branches.create.name")}
                onChange={this.handleNameChange}
                value={name ? name : ""}
                validationError={this.state.nameValidationError}
                errorMessage={t("validation.branch.nameInvalid")}
              />
            </div>
          </div>
          <div className="columns">
            <div className="column">
              <SubmitButton
                disabled={!this.isValid()}
                loading={loading}
                label={t("branches.create.submit")}
              />
            </div>
          </div>
        </form>
      </>
    );
  }

  handleSourceChange = (source: string) => {
    this.setState({
      ...this.state,
      source
    });
  };

  handleNameChange = (name: string) => {
    this.setState({
      nameValidationError: !validator.isNameValid(name),
      ...this.state,
      name
    });
  };
}

export default translate("repos")(BranchForm);
