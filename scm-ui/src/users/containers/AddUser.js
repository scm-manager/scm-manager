//@flow
import React from "react";
import { connect } from "react-redux";
import UserForm from "./UserForm";
import type { User } from "../types/User";

import { addUser } from "../modules/users";

type Props = {
  addUser: User => void,
  loading?: boolean
};

class AddUser extends React.Component<Props> {
  render() {
    const addUser = this.props.addUser;

    return (
      <div>
        <UserForm
          submitForm={user => addUser(user)}
          loading={this.props.loading}
        />
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
  if (state.users && state.users.users) {
    return {
      loading: state.users.users.loading
    };
  }
  return {};
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(AddUser);
