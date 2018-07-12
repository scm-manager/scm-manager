// @flow
import React from "react";
import type {
  User
} from "../types/User";
import {
  confirmAlert
} from '../../components/ConfirmAlert';

type Props = {
  user: User,
  deleteUser: (link: string) => void,
};

class DeleteUserButton extends React.Component < Props > {

  deleteUser = () => {
    this.props.deleteUser(this.props.user._links.delete.href);
  };

  confirmDelete = () => {
    confirmAlert({
      title: 'Delete user',
      message: 'Do you really want to delete the user?',
      buttons: [{
          label: 'Yes',
          onClick: () => this.deleteUser()
        },
        {
          label: 'No',
          onClick: () => null
        }
      ]
    })
  }

  isDeletable = () => {
    return this.props.user._links.delete;
  };

  render() {
    if (!this.isDeletable()) {
      return;
    }
    return ( <
      button type = "button"
      onClick = {
        (e) => {
          this.confirmDelete()
        }
      } >
      Delete User <
      /button>
    );
  }
}

export default DeleteUserButton;
