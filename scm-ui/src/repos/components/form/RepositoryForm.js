// @flow
import React from "react";
import { translate } from "react-i18next";
import {
  Subtitle,
  InputField,
  Select,
  SubmitButton,
  Textarea
} from "@scm-manager/ui-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import type { Repository, RepositoryType } from "@scm-manager/ui-types";
import * as validator from "./repositoryValidation";

type Props = {
  submitForm: Repository => void,
  repository?: Repository,
  repositoryTypes: RepositoryType[],
  namespaceStrategy: string,
  loading?: boolean,
  t: string => string
};

type State = {
  repository: Repository,
  namespaceValidationError: boolean,
  nameValidationError: boolean,
  contactValidationError: boolean
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
        _links: {}
      },
      namespaceValidationError: false,
      nameValidationError: false,
      contactValidationError: false
    };
  }

  componentDidMount() {
    const { repository } = this.props;
    if (repository) {
      this.setState({ repository: { ...repository } });
    }
  }

  isFalsy(value) {
    if (!value) {
      return true;
    }
    return false;
  }

  isValid = () => {
    const repository = this.state.repository;
    return !(
      this.state.namespaceValidationError ||
      this.state.nameValidationError ||
      this.state.contactValidationError ||
      this.isFalsy(repository.name)
    );
  };

  submit = (event: Event) => {
    event.preventDefault();
    if (this.isValid()) {
      this.props.submitForm(this.state.repository);
    }
  };

  isCreateMode = () => {
    return !this.props.repository;
  };

  render() {
    const { loading, t } = this.props;
    const repository = this.state.repository;

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
          />

          <Textarea
            label={t("repository.description")}
            onChange={this.handleDescriptionChange}
            value={repository ? repository.description : ""}
            helpText={t("help.descriptionHelpText")}
          />
          <SubmitButton
            disabled={!this.isValid()}
            loading={loading}
            label={t("repositoryForm.submit")}
          />
        </form>
      </>
    );
  }

  createSelectOptions(repositoryTypes: RepositoryType[]) {
    return repositoryTypes.map(repositoryType => {
      return {
        label: repositoryType.displayName,
        value: repositoryType.name
      };
    });
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

    if (namespaceStrategy === "CustomNamespaceStrategy") {
      return <InputField {...props} />;
    }

    return (
      <ExtensionPoint
        name="repos.create.namespace"
        props={props}
        renderAll={false}
      />
    );
  };

  renderCreateOnlyFields() {
    if (!this.isCreateMode()) {
      return null;
    }
    const { repositoryTypes, t } = this.props;
    const repository = this.state.repository;
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
        <Select
          label={t("repository.type")}
          onChange={this.handleTypeChange}
          value={repository ? repository.type : ""}
          options={this.createSelectOptions(repositoryTypes)}
          helpText={t("help.typeHelpText")}
        />
      </>
    );
  }

  handleNamespaceChange = (namespace: string) => {
    this.setState({
      namespaceValidationError: !validator.isNameValid(namespace),
      repository: { ...this.state.repository, namespace }
    });
  };

  handleNameChange = (name: string) => {
    this.setState({
      nameValidationError: !validator.isNameValid(name),
      repository: { ...this.state.repository, name }
    });
  };

  handleTypeChange = (type: string) => {
    this.setState({
      repository: { ...this.state.repository, type }
    });
  };

  handleContactChange = (contact: string) => {
    this.setState({
      contactValidationError: !validator.isContactValid(contact),
      repository: { ...this.state.repository, contact }
    });
  };

  handleDescriptionChange = (description: string) => {
    this.setState({
      repository: { ...this.state.repository, description }
    });
  };
}

export default translate("repos")(RepositoryForm);
