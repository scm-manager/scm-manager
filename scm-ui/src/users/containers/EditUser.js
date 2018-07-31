//@flow
import React from "react";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import UserForm from "./../components/UserForm";
import type { User } from "../types/User";
import {
  modifyUser,
  isModifyUserPending,
  getModifyUserFailure
} from "../modules/users";
import type { History } from "history";
import ErrorNotification from "../../components/ErrorNotification";

type Props = {
  loading: boolean,
  error: Error,

  // dispatch functions
  modifyUser: (user: User, callback?: () => void) => void,

  // context objects
  user: User,
  history: History
};

class EditUser extends React.Component<Props> {
  userModified = (user: User) => () => {
    this.props.history.push(`/user/${user.name}`);
  };

  modifyUser = (user: User) => {
    this.props.modifyUser(user, this.userModified(user));
  };

  render() {
    const { user, loading, error } = this.props;
    return (
      <div>
        <ErrorNotification error={error} />
        <UserForm
          submitForm={user => this.modifyUser(user)}
          user={user}
          loading={loading}
        />
      </div>
    );
  }
}

const mapDispatchToProps = dispatch => {
  return {
    modifyUser: (user: User, callback?: () => void) => {
      dispatch(modifyUser(user, callback));
    }
  };
};

const mapStateToProps = (state, ownProps) => {
  const loading = isModifyUserPending(state, ownProps.user.name);
  const error = getModifyUserFailure(state, ownProps.user.name);
  return {
    loading,
    error
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withRouter(EditUser));
