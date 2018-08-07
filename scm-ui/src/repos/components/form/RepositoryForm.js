// @flow
import React from "react";
import { translate } from "react-i18next";
import { InputField, Select } from "../../../components/forms/index";
import { SubmitButton } from "../../../components/buttons/index";
import type { Repository } from "../../types/Repositories";
import * as validator from "./repositoryValidation";
import type { RepositoryType } from "../../types/RepositoryTypes";
import Textarea from "../../../components/forms/Textarea";

type Props = {
  submitForm: Repository => void,
  repository?: Repository,
  repositoryTypes: RepositoryType[],
  loading?: boolean,
  t: string => string
};

type State = {
  repository: Repository,
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
      nameValidationError: false,
      contactValidationError: false,
      descriptionValidationError: false
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

    return (
      <form onSubmit={this.submit}>
        {this.renderCreateOnlyFields()}
        <InputField
          label={t("repository.contact")}
          onChange={this.handleContactChange}
          value={repository ? repository.contact : ""}
          validationError={this.state.contactValidationError}
          errorMessage={t("validation.contact-invalid")}
        />

        <Textarea
          label={t("repository.description")}
          onChange={this.handleDescriptionChange}
          value={repository ? repository.description : ""}
        />
        <SubmitButton
          disabled={!this.isValid()}
          loading={loading}
          label={t("repository-form.submit")}
        />
      </form>
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

  renderCreateOnlyFields() {
    if (!this.isCreateMode()) {
      return null;
    }
    const { repositoryTypes, t } = this.props;
    const repository = this.state.repository;
    return (
      <div>
        <InputField
          label={t("repository.name")}
          onChange={this.handleNameChange}
          value={repository ? repository.name : ""}
          validationError={this.state.nameValidationError}
          errorMessage={t("validation.name-invalid")}
        />
        <Select
          label={t("repository.type")}
          onChange={this.handleTypeChange}
          value={repository ? repository.type : ""}
          options={this.createSelectOptions(repositoryTypes)}
        />
      </div>
    );
  }

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
