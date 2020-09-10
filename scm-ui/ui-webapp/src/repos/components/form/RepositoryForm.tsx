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
import React from "react";
import styled from "styled-components";
import { WithTranslation, withTranslation } from "react-i18next";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { Repository, RepositoryCreation, RepositoryType } from "@scm-manager/ui-types";
import { Checkbox, InputField, Level, Select, SubmitButton, Subtitle, Textarea } from "@scm-manager/ui-components";
import * as validator from "./repositoryValidation";
import { CUSTOM_NAMESPACE_STRATEGY } from "../../modules/repos";

const CheckboxWrapper = styled.div`
  margin-top: 2em;
  flex: 1;
`;

const SelectWrapper = styled.div`
  flex: 1;
`;

const SpaceBetween = styled.div`
  display: flex;
  justify-content: space-between;
`;

type Props = WithTranslation & {
  submitForm: (repo: RepositoryCreation, shouldInit: boolean) => void;
  repository?: Repository;
  repositoryTypes?: RepositoryType[];
  namespaceStrategy?: string;
  loading?: boolean;
  indexResources: any;
};

type State = {
  repository: RepositoryCreation;
  initRepository: boolean;
  namespaceValidationError: boolean;
  nameValidationError: boolean;
  contactValidationError: boolean;
};

class RepositoryForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      repository: {
        name: "",
        namespace: "",
        type: "",
        contact: "",
        description: "",
        contextEntries: {},
        _links: {}
      },
      initRepository: false,
      namespaceValidationError: false,
      nameValidationError: false,
      contactValidationError: false
    };
  }

  componentDidMount() {
    const { repository } = this.props;
    if (repository) {
      this.setState({
        repository: {
          ...repository,
          contextEntries: {}
        }
      });
    }
  }

  isFalsy(value: string) {
    return !value;
  }

  isValid = () => {
    const { namespaceStrategy } = this.props;
    const { repository } = this.state;
    return !(
      this.state.namespaceValidationError ||
      this.state.nameValidationError ||
      this.state.contactValidationError ||
      this.isFalsy(repository.name) ||
      (namespaceStrategy === CUSTOM_NAMESPACE_STRATEGY && this.isFalsy(repository.namespace))
    );
  };

  submit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (this.isValid()) {
      this.props.submitForm(this.state.repository, this.state.initRepository);
    }
  };

  isCreateMode = () => {
    return !this.props.repository;
  };

  isModifiable = () => {
    return !!this.props.repository && !!this.props.repository._links.update;
  };

  toggleInitCheckbox = () => {
    this.setState({
      initRepository: !this.state.initRepository
    });
  };

  setCreationContextEntry = (key: string, value: any) => {
    this.setState({
      repository: {
        ...this.state.repository,
        contextEntries: {
          ...this.state.repository.contextEntries,
          [key]: value
        }
      }
    });
  };

  render() {
    const { loading, t } = this.props;
    const repository = this.state.repository;

    const disabled = !this.isModifiable() && !this.isCreateMode();

    const submitButton = disabled ? null : (
      <Level right={<SubmitButton disabled={!this.isValid()} loading={loading} label={t("repositoryForm.submit")} />} />
    );

    let subtitle = null;
    if (this.props.repository) {
      // edit existing repo
      subtitle = <Subtitle subtitle={t("repositoryForm.subtitle")} />;
    }

    return (
      <>
        {subtitle}
        <form onSubmit={this.submit}>
          {this.renderCreateOnlyFields()}
          <InputField
            label={t("repository.contact")}
            onChange={this.handleContactChange}
            value={repository ? repository.contact : ""}
            validationError={this.state.contactValidationError}
            errorMessage={t("validation.contact-invalid")}
            helpText={t("help.contactHelpText")}
            disabled={disabled}
          />

          <Textarea
            label={t("repository.description")}
            onChange={this.handleDescriptionChange}
            value={repository ? repository.description : ""}
            helpText={t("help.descriptionHelpText")}
            disabled={disabled}
          />
          {submitButton}
        </form>
      </>
    );
  }

  createSelectOptions(repositoryTypes?: RepositoryType[]) {
    if (repositoryTypes) {
      return repositoryTypes.map(repositoryType => {
        return {
          label: repositoryType.displayName,
          value: repositoryType.name
        };
      });
    }
    return [];
  }

  renderNamespaceField = () => {
    const { namespaceStrategy, t } = this.props;
    const repository = this.state.repository;
    const props = {
      label: t("repository.namespace"),
      helpText: t("help.namespaceHelpText"),
      value: repository ? repository.namespace : "",
      onChange: this.handleNamespaceChange,
      errorMessage: t("validation.namespace-invalid"),
      validationError: this.state.namespaceValidationError
    };

    if (namespaceStrategy === CUSTOM_NAMESPACE_STRATEGY) {
      return <InputField {...props} />;
    }

    return <ExtensionPoint name="repos.create.namespace" props={props} renderAll={false} />;
  };

  renderCreateOnlyFields() {
    if (!this.isCreateMode()) {
      return null;
    }
    const { repositoryTypes, indexResources, t } = this.props;
    const repository = this.state.repository;
    const extensionProps = {
      repository,
      setCreationContextEntry: this.setCreationContextEntry,
      indexResources
    };
    return (
      <>
        {this.renderNamespaceField()}
        <InputField
          label={t("repository.name")}
          onChange={this.handleNameChange}
          value={repository ? repository.name : ""}
          validationError={this.state.nameValidationError}
          errorMessage={t("validation.name-invalid")}
          helpText={t("help.nameHelpText")}
        />
        <SpaceBetween>
          <SelectWrapper>
            <Select
              label={t("repository.type")}
              onChange={this.handleTypeChange}
              value={repository ? repository.type : ""}
              options={this.createSelectOptions(repositoryTypes)}
              helpText={t("help.typeHelpText")}
            />
          </SelectWrapper>
          <CheckboxWrapper>
            <Checkbox
              label={t("repositoryForm.initializeRepository")}
              checked={this.state.initRepository}
              onChange={this.toggleInitCheckbox}
              helpText={t("help.initializeRepository")}
            />
            {this.state.initRepository && (
              <ExtensionPoint name="repos.create.initialize" props={extensionProps} renderAll={true} />
            )}
          </CheckboxWrapper>
        </SpaceBetween>
      </>
    );
  }

  handleNamespaceChange = (namespace: string) => {
    this.setState({
      namespaceValidationError: !validator.isNameValid(namespace),
      repository: {
        ...this.state.repository,
        namespace
      }
    });
  };

  handleNameChange = (name: string) => {
    this.setState({
      nameValidationError: !validator.isNameValid(name),
      repository: {
        ...this.state.repository,
        name
      }
    });
  };

  handleTypeChange = (type: string) => {
    this.setState({
      repository: {
        ...this.state.repository,
        type
      }
    });
  };

  handleContactChange = (contact: string) => {
    this.setState({
      contactValidationError: !validator.isContactValid(contact),
      repository: {
        ...this.state.repository,
        contact
      }
    });
  };

  handleDescriptionChange = (description: string) => {
    this.setState({
      repository: {
        ...this.state.repository,
        description
      }
    });
  };
}

export default withTranslation("repos")(RepositoryForm);
