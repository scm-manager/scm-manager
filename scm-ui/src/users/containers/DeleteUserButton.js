// @flow
import React from "react";
import type { User } from "../types/User";

type Props = {
  user: User,
  deleteUser: (link: string) => void
};

class DeleteUser extends React.Component<Props> {
  deleteUser = () => {
    this.props.deleteUser(this.props.user._links.delete.href);
  };

  isDeletable = () => {
    return this.props.user._links.delete;
  };

  render() {
    if (!this.isDeletable()) {
      return;
    }
    return (
      <button type="button" onClick={this.deleteUser}>
        Delete User
      </button>
    );
  }
}

export default DeleteUser;
