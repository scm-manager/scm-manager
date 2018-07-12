// @flow
import React from "react";
import type { User } from "../types/User";

type Props = {
  user: User,
  deleteUser: (link: string) => void,
};

class DeleteUserButton extends React.Component<Props> {

  deleteUser = () => {
    this.props.deleteUser(this.props.user._links.delete.href);
  };

  isDeletable = () => {
    return this.props.user._links.delete;
  };

  render() {
    return (

      <button type="button" onClick={(e) => { if (window.confirm('Are you sure you wish to delete this item?')) this.deleteUser() } }>
        Delete User       
      </button>
    );
  }
}

export default DeleteUserButton;
