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
import React, { FormEvent } from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Branch, BranchCreation, Repository } from "@scm-manager/ui-types";
import { InputField, Level, Select, SubmitButton, validation as validator } from "@scm-manager/ui-components";
import { orderBranches } from "../util/orderBranches";

type Props = WithTranslation & {
  submitForm: (p: BranchCreation) => void;
  repository: Repository;
  branches: Branch[];
  loading?: boolean;
  transmittedName?: string;
  disabled?: boolean;
};

type State = {
  source?: string;
  name?: string;
  nameValidationError: boolean;
};

class BranchForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      nameValidationError: false,
      name: props.transmittedName,
    };
  }

  isFalsy(value?: string) {
    return !value;
  }

  isValid = () => {
    const { source, name } = this.state;
    return !(this.state.nameValidationError || this.isFalsy(source) || this.isFalsy(name));
  };

  submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (this.isValid()) {
      this.props.submitForm({
        name: this.state.name!,
        parent: this.state.source!,
      });
    }
  };

  render() {
    const { t, branches, loading, transmittedName, disabled } = this.props;
    const { name } = this.state;
    orderBranches(branches);
    const options = branches.map((branch) => ({
      label: branch.name,
      value: branch.name,
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
                disabled={disabled}
              />
              <InputField
                name="name"
                label={t("branches.create.name")}
                onChange={this.handleNameChange}
                value={name ? name : ""}
                validationError={this.state.nameValidationError}
                errorMessage={t("validation.branch.nameInvalid")}
                disabled={!!transmittedName || disabled}
              />
            </div>
          </div>
          <div className="columns">
            <div className="column">
              <Level
                right={
                  <SubmitButton
                    disabled={disabled || !this.isValid()}
                    loading={loading}
                    label={t("branches.create.submit")}
                  />
                }
              />
            </div>
          </div>
        </form>
      </>
    );
  }

  handleSourceChange = (source: string) => {
    this.setState({
      source,
    });
  };

  handleNameChange = (name: string) => {
    this.setState({
      nameValidationError: !validator.isBranchValid(name),
      name,
    });
  };
}

export default withTranslation("repos")(BranchForm);
