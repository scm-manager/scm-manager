//@flow
import React from "react";
import {connect} from "react-redux";
import GroupForm from "../components/GroupForm";
import {getModifyGroupFailure, isModifyGroupPending, modifyGroup, modifyGroupReset} from "../modules/groups";
import type {History} from "history";
import {withRouter} from "react-router-dom";
import type {Group} from "@scm-manager/ui-types";
import {ErrorNotification} from "@scm-manager/ui-components";

type Props = {
  group: Group,
  modifyGroup: (group: Group, callback?: () => void) => void,
  modifyGroupReset: Group => void,
  fetchGroup: (name: string) => void,
  history: History,
  loading?: boolean,
  error: Error
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
    const url = "http://localhost:8081/scm/api/v2/autocomplete/users?q=";
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
      </div>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const loading = isModifyGroupPending(state, ownProps.group.name);
  const error = getModifyGroupFailure(state, ownProps.group.name);
  return {
    loading,
    error
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
