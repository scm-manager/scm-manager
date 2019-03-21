//@flow
import React from "react";
import { translate } from "react-i18next";
import {
  Subtitle,
  AutocompleteAddEntryToTableField,
  LabelWithHelpIcon,
  MemberNameTable,
  InputField,
  SubmitButton,
  Textarea,
  Checkbox
} from "@scm-manager/ui-components";
import type { Group, SelectValue } from "@scm-manager/ui-types";

import * as validator from "./groupValidation";

type Props = {
  t: string => string,
  submitForm: Group => void,
  loading?: boolean,
  group?: Group,
  loadUserSuggestions: string => any
};

type State = {
  group: Group,
  nameValidationError: boolean
};

class GroupForm extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {
      group: {
        name: "",
        description: "",
        _embedded: {
          members: []
        },
        _links: {},
        members: [],
        type: ""
      },
      nameValidationError: false
    };
  }

  componentDidMount() {
    const { group } = this.props;
    if (group) {
      this.setState({ ...this.state, group: { ...group } });
    }
  }

  isFalsy(value) {
    if (!value) {
      return true;
    }
    return false;
  }

  isValid = () => {
    const group = this.state.group;
    return !(this.state.nameValidationError || this.isFalsy(group.name));
  };

  submit = (event: Event) => {
    event.preventDefault();
    if (this.isValid()) {
      const { group } = this.state;
      if (group.external) {
        group.members = [];
      }
      this.props.submitForm(group);
    }
  };

  renderMemberfields = (group: Group) => {
    if (group.external) {
      return null;
    }

    const { loadUserSuggestions, t } = this.props;
    return (
      <>
        <LabelWithHelpIcon
          label={t("group.members")}
          helpText={t("groupForm.help.memberHelpText")}
        />
        <MemberNameTable
          members={group.members}
          memberListChanged={this.memberListChanged}
        />
        <AutocompleteAddEntryToTableField
          addEntry={this.addMember}
          disabled={false}
          buttonLabel={t("add-member-button.label")}
          fieldLabel={t("add-member-textfield.label")}
          errorMessage={t("add-member-textfield.error")}
          loadSuggestions={loadUserSuggestions}
          placeholder={t("add-member-autocomplete.placeholder")}
          loadingMessage={t("add-member-autocomplete.loading")}
          noOptionsMessage={t("add-member-autocomplete.no-options")}
        />
      </>
    );
  };

  renderExternalField = (group: Group) => {
    const { t } = this.props;
    if (this.isExistingGroup()) {
      return null;
    }
    return (
      <Checkbox
        label={t("group.external")}
        checked={group.external}
        helpText={t("groupForm.help.externalHelpText")}
        onChange={this.handleExternalChange}
      />
    );
  };

  isExistingGroup = () => !! this.props.group;

  render() {
    const { loading, t } = this.props;
    const { group } = this.state;
    let nameField = null;
    let subtitle = null;
    if (!this.isExistingGroup()) {
      // create new group
      nameField = (
        <InputField
          label={t("group.name")}
          errorMessage={t("groupForm.nameError")}
          onChange={this.handleGroupNameChange}
          value={group.name}
          validationError={this.state.nameValidationError}
          helpText={t("groupForm.help.nameHelpText")}
        />
      );
    } else if (group.external) {
      subtitle = <Subtitle subtitle={t("groupForm.externalSubtitle")} />;
    } else {
      subtitle = <Subtitle subtitle={t("groupForm.subtitle")} />;
    }

    return (
      <>
        {subtitle}
        <form onSubmit={this.submit}>
          {nameField}
          <Textarea
            label={t("group.description")}
            errorMessage={t("groupForm.descriptionError")}
            onChange={this.handleDescriptionChange}
            value={group.description}
            validationError={false}
            helpText={t("groupForm.help.descriptionHelpText")}
          />
          {this.renderExternalField(group)}
          {this.renderMemberfields(group)}
          <SubmitButton
            disabled={!this.isValid()}
            label={t("groupForm.submit")}
            loading={loading}
          />
        </form>
      </>
    );
  }

  memberListChanged = membernames => {
    this.setState({
      ...this.state,
      group: {
        ...this.state.group,
        members: membernames
      }
    });
  };

  addMember = (value: SelectValue) => {
    if (this.isMember(value.value.id)) {
      return;
    }

    this.setState({
      ...this.state,
      group: {
        ...this.state.group,
        members: [...this.state.group.members, value.value.id]
      }
    });
  };

  isMember = (membername: string) => {
    return this.state.group.members.includes(membername);
  };

  handleGroupNameChange = (name: string) => {
    this.setState({
      nameValidationError: !validator.isNameValid(name),
      group: { ...this.state.group, name }
    });
  };

  handleDescriptionChange = (description: string) => {
    this.setState({
      group: { ...this.state.group, description }
    });
  };

  handleExternalChange = (external: boolean) => {
    this.setState({
      group: { ...this.state.group, external }
    });
  };
}

export default translate("groups")(GroupForm);
