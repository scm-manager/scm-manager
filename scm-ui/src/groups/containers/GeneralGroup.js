//@flow
import React from "react";
import { connect } from "react-redux";
import GroupForm from "../components/GroupForm";
import {
  modifyGroup,
  deleteGroup,
  getModifyGroupFailure,
  isModifyGroupPending,
  getDeleteGroupFailure,
  isDeleteGroupPending,
  modifyGroupReset
} from "../modules/groups";
import type { History } from "history";
import { withRouter } from "react-router-dom";
import type { Group } from "@scm-manager/ui-types";
import { ErrorNotification } from "@scm-manager/ui-components";
import { getUserAutoCompleteLink } from "../../modules/indexResource";
import DeleteGroup from "../components/DeleteGroup";

type Props = {
  group: Group,
  fetchGroup: (name: string) => void,
  modifyGroup: (group: Group, callback?: () => void) => void,
  modifyGroupReset: Group => void,
  deleteGroup: (group: Group, callback?: () => void) => void,
  autocompleteLink: string,
  history: History,
  loading?: boolean,
  error: Error
};

class GeneralGroup extends React.Component<Props> {
  componentDidMount() {
    const { group, modifyGroupReset } = this.props;
    modifyGroupReset(group);
  }

  groupModified = (group: Group) => () => {
    this.props.history.push(`/group/${group.name}`);
  };

  modifyGroup = (group: Group) => {
    this.props.modifyGroup(group, this.groupModified(group));
  };

  deleteGroup = (group: Group) => {
    this.props.deleteGroup(group, this.groupDeleted);
  };

  groupDeleted = () => {
    this.props.history.push("/groups");
  };

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

  render() {
    const { group, loading, error } = this.props;
    return (
      <div>
        <ErrorNotification error={error} />
        <GroupForm
          group={group}
          submitForm={group => {
            this.modifyGroup(group);
          }}
          loading={loading}
          loadUserSuggestions={this.loadUserAutocompletion}
        />
        <hr />
        <DeleteGroup
          group={group}
          deleteGroup={this.deleteGroup} />
      </div>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const loading = isModifyGroupPending(state, ownProps.group.name) || isDeleteGroupPending(state, ownProps.group.name);
  const error = getModifyGroupFailure(state, ownProps.group.name) || getDeleteGroupFailure(state, ownProps.group.name);
  const autocompleteLink = getUserAutoCompleteLink(state);
  return {
    loading,
    error,
    autocompleteLink
  };
};

const mapDispatchToProps = dispatch => {
  return {
    modifyGroup: (group: Group, callback?: () => void) => {
      dispatch(modifyGroup(group, callback));
    },
    modifyGroupReset: (group: Group) => {
      dispatch(modifyGroupReset(group));
    },
    deleteGroup: (group: Group, callback?: () => void) => {
      dispatch(deleteGroup(group, callback));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withRouter(GeneralGroup));
