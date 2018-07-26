//@flow
import React from "react";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import UserForm from "./../components/UserForm";
import type { User } from "../types/User";
import { modifyUser } from "../modules/users";
import type { History } from "history";

type Props = {
  user: User,
  modifyUser: (user: User, callback?: () => void) => void,
  loading: boolean,
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
    const { user } = this.props;
    return <UserForm submitForm={user => this.modifyUser(user)} user={user} />;
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
  return {};
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withRouter(EditUser));
