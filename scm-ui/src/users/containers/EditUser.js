//@flow
import React from "react";
import { connect } from "react-redux";
import UserForm from "./UserForm";
import type { User } from "../types/User";

import {
  updateUser,
  deleteUser,
  editUser,
  fetchUser,
  getUsersFromState
} from "../modules/users";
import { Route, Link } from "react-router-dom";

type Props = {
  name: string,
  fetchUser: string => void,
  usersByNames: Map<string, any>,
  updateUser: User => void
};

class EditUser extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchUser(this.props.name);
  }

  render() {
    const submitUser = this.props.updateUser;

    const { usersByNames, name } = this.props;

    if (!usersByNames || usersByNames[name].loading) {
      return <div>Loading...</div>;
    } else {
      const user = usersByNames[name].entry;
      return (
        <div>
          <UserForm submitForm={user => submitUser(user)} user={user} />
        </div>
      );
    }
  }
}

const mapDispatchToProps = dispatch => {
  return {
    fetchUser: (name: string) => {
      dispatch(fetchUser(name));
    },
    updateUser: (user: User) => {
      dispatch(updateUser(user));
    }
  };
};

const mapStateToProps = (state, ownProps) => {
  return {
    usersByNames: state.users.usersByNames,
    name: ownProps.match.params.name
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(EditUser);
