// @flow
import React from "react";
import { deleteUser } from '../modules/users';
import {connect} from "react-redux";

type Props = {
  user: any,
  deleteUser: (username: string) => void
};

class DeleteUser extends React.Component<Props> {

  deleteUser = () => {
    this.props.deleteUser(this.props.user.name);
  };

  render() {
    if(this.props.user._links.delete) {
      return (
        <button type="button" onClick={this.deleteUser}>
          Delete User
        </button>

      );
    }
  }
}

const mapStateToProps = state => {
  return {
    users: state.users.users
  };
};

const mapDispatchToProps = dispatch => {
  return {
    deleteUser: (username: string) => {
      dispatch(deleteUser(username));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(DeleteUser);
