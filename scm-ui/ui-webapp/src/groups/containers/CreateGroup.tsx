import React from "react";
import { connect } from "react-redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { History } from "history";
import { Group } from "@scm-manager/ui-types";
import { Page } from "@scm-manager/ui-components";
import { getGroupsLink, getUserAutoCompleteLink } from "../../modules/indexResource";
import { createGroup, isCreateGroupPending, getCreateGroupFailure, createGroupReset } from "../modules/groups";
import GroupForm from "../components/GroupForm";

type Props = WithTranslation & {
  createGroup: (link: string, group: Group, callback?: () => void) => void;
  history: History;
  loading?: boolean;
  error?: Error;
  resetForm: () => void;
  createLink: string;
  autocompleteLink: string;
};

class CreateGroup extends React.Component<Props> {
  componentDidMount() {
    this.props.resetForm();
  }

  render() {
    const { t, loading, error } = this.props;
    return (
      <Page title={t("add-group.title")} subtitle={t("add-group.subtitle")} error={error}>
        <div>
          <GroupForm
            submitForm={group => this.createGroup(group)}
            loading={loading}
            loadUserSuggestions={this.loadUserAutocompletion}
          />
        </div>
      </Page>
    );
  }

  loadUserAutocompletion = (inputValue: string) => {
    const url = this.props.autocompleteLink + "?q=";
    return fetch(url + inputValue)
      .then(response => response.json())
      .then(json => {
        return json.map(element => {
          return {
            value: element,
            label: `${element.displayName} (${element.id})`
          };
        });
      });
  };
  groupCreated = (group: Group) => {
    this.props.history.push("/group/" + group.name);
  };
  createGroup = (group: Group) => {
    this.props.createGroup(this.props.createLink, group, () => this.groupCreated(group));
  };
}

const mapDispatchToProps = dispatch => {
  return {
    createGroup: (link: string, group: Group, callback?: () => void) => dispatch(createGroup(link, group, callback)),
    resetForm: () => {
      dispatch(createGroupReset());
    }
  };
};

const mapStateToProps = state => {
  const loading = isCreateGroupPending(state);
  const error = getCreateGroupFailure(state);
  const createLink = getGroupsLink(state);
  const autocompleteLink = getUserAutoCompleteLink(state);
  return {
    createLink,
    loading,
    error,
    autocompleteLink
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withTranslation("groups")(CreateGroup));
