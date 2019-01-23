//@flow
import React from "react";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import UserForm from "../components/UserForm";
import DeleteUser from "../components/DeleteUser";
import type { User } from "@scm-manager/ui-types";
import {
  modifyUser,
  deleteUser,
  isModifyUserPending,
  getModifyUserFailure,
  modifyUserReset,
  isDeleteUserPending,
  getDeleteUserFailure
} from "../modules/users";
import type { History } from "history";
import { ErrorNotification } from "@scm-manager/ui-components";

type Props = {
  loading: boolean,
  error: Error,

  // dispatch functions
  modifyUser: (user: User, callback?: () => void) => void,
  modifyUserReset: User => void,
  deleteUser: (user: User, callback?: () => void) => void,

  // context objects
  user: User,
  history: History
};

class GeneralUser extends React.Component<Props> {
  componentDidMount() {
    const { modifyUserReset, user } = this.props;
    modifyUserReset(user);
  }

  userModified = (user: User) => () => {
    this.props.history.push(`/user/${user.name}`);
  };

  modifyUser = (user: User) => {
    this.props.modifyUser(user, this.userModified(user));
  };

  userDeleted = () => {
    this.props.history.push("/users");
  };

  deleteUser = (user: User) => {
    this.props.deleteUser(user, this.userDeleted);
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
        <hr />
        <DeleteUser user={user} deleteUser={this.deleteUser} />
      </div>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const loading = isModifyUserPending(state, ownProps.user.name) || isDeleteUserPending(state, ownProps.user.name);
  const error = getModifyUserFailure(state, ownProps.user.name) || getDeleteUserFailure(state, ownProps.user.name);
  return {
    loading,
    error
  };
};

const mapDispatchToProps = dispatch => {
  return {
    modifyUser: (user: User, callback?: () => void) => {
      dispatch(modifyUser(user, callback));
    },
    modifyUserReset: (user: User) => {
      dispatch(modifyUserReset(user));
    },
    deleteUser: (user: User, callback?: () => void) => {
      dispatch(deleteUser(user, callback));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withRouter(GeneralUser));
