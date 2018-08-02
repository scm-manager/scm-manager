//@flow
import React from "react";
import { connect } from "react-redux";
import GroupForm from "../components/GroupForm";
import { modifyGroup } from "../modules/groups"
import type { History } from "history";
import { withRouter } from "react-router-dom";
import type { Group } from "../types/Group"
import { isModifyGroupPending, getModifyGroupFailure } from "../modules/groups"
import ErrorNotification from "../../components/ErrorNotification";

type Props = {
  group: Group,
  modifyGroup: (group: Group, callback?: () => void) => void,
  history: History,
  loading?: boolean,
  error: Error
};

class EditGroup extends React.Component<Props> {
  groupModified = (group: Group) => () => {
    this.props.history.push(`/group/${group.name}`)
  }

  modifyGroup = (group: Group) => {
    this.props.modifyGroup(group, this.groupModified(group));
  }  

  render() {
    const { group, loading, error } = this.props;
    return <div>
      <ErrorNotification error={error} />
      <GroupForm group={group} submitForm={(group) =>{this.modifyGroup(group)}} loading={loading}/>
    </div>
  }
}

const mapStateToProps = (state, ownProps) => {
  const loading = isModifyGroupPending(state, ownProps.group.name)
  const error = getModifyGroupFailure(state, ownProps.group.name)
  return {
    loading,
    error
  };
};

const mapDispatchToProps = (dispatch) => {
  return {
   modifyGroup: (group: Group, callback?: () => void) => {
     dispatch(modifyGroup(group, callback))
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withRouter(EditGroup));