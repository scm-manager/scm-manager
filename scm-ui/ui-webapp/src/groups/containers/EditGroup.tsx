import React from "react";
import { connect } from "react-redux";
import GroupForm from "../components/GroupForm";
import { modifyGroup, getModifyGroupFailure, isModifyGroupPending, modifyGroupReset } from "../modules/groups";
import { History } from "history";
import { withRouter } from "react-router-dom";
import { Group } from "@scm-manager/ui-types";
import { ErrorNotification } from "@scm-manager/ui-components";
import { getUserAutoCompleteLink } from "../../modules/indexResource";
import DeleteGroup from "./DeleteGroup";
import { apiClient } from "@scm-manager/ui-components/src";

type Props = {
  group: Group;
  fetchGroup: (name: string) => void;
  modifyGroup: (group: Group, callback?: () => void) => void;
  modifyGroupReset: (p: Group) => void;
  autocompleteLink: string;
  history: History;
  loading?: boolean;
  error: Error;
};

class EditGroup extends React.Component<Props> {
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

  loadUserAutocompletion = (inputValue: string) => {
    const url = this.props.autocompleteLink + "?q=";
    return apiClient
      .get(url + inputValue)
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
    const { loading, error, group } = this.props;
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
        <DeleteGroup group={group} />
      </div>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const loading = isModifyGroupPending(state, ownProps.group.name);
  const error = getModifyGroupFailure(state, ownProps.group.name);
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
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withRouter(EditGroup));
