//@flow
import React from "react";
import { connect } from "react-redux";
import UserForm from "./UserForm";
import type { User } from "../types/User";

import { addUser } from "../modules/users";
import { Route, Link } from "react-router-dom";

type Props = {
  addUser: User => void
};

class AddUser extends React.Component<Props> {
  render() {
    const addUser = this.props.addUser;

    return (
      <div>
        <UserForm submitForm={user => addUser(user)} />
      </div>
    );
  }
}

const mapDispatchToProps = dispatch => {
  return {
    addUser: (user: User) => {
      dispatch(addUser(user));
    }
  };
};

const mapStateToProps = (state, ownProps) => {
  return {};
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(AddUser);
