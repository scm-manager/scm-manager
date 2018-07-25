//@flow
import React from "react";
import { connect } from "react-redux";
import UserForm from "./../components/UserForm";
import type { User } from "../types/User";
import { modifyUser } from "../modules/users";

type Props = {
  user: User,
  updateUser: User => void,
  loading: boolean
};

class EditUser extends React.Component<Props> {
  render() {
    const { user, updateUser } = this.props;
    return <UserForm submitForm={user => updateUser(user)} user={user} />;
  }
}

const mapDispatchToProps = dispatch => {
  return {
    updateUser: (user: User) => {
      dispatch(modifyUser(user));
    }
  };
};

const mapStateToProps = (state, ownProps) => {
  return {};
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(EditUser);
