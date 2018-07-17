// @flow
import React from "react";
import type { User } from "../types/User";
import { confirmAlert } from '../../components/ConfirmAlert';

type Props = {
  user: User,
  confirmDialog?: boolean,
  deleteUser: (user: User) => void,
};

class DeleteUserButton extends React.Component<Props> {

  static defaultProps = {
    confirmDialog: true
  };

  deleteUser = () => {
    this.props.deleteUser(this.props.user);
  };

  confirmDelete = () =>{
    confirmAlert({ 
      title: 'Delete user',
      message: 'Do you really want to delete the user?',
      buttons: [
        {
          label: 'Yes',
          onClick: () => this.deleteUser()
        },
        {
          label: 'No',
          onClick: () => null
        }
      ]
    });
  };

  isDeletable = () => {
    return this.props.user._links.delete;
  };

  render() {
    const { confirmDialog } = this.props;
    const action = confirmDialog ? this.confirmDelete : this.deleteUser;

    if (!this.isDeletable()) {
      return;
    }
    return (
      <button type="button" onClick={(e) => { action() } }>
        Delete User       
      </button>
    );
  }
}

export default DeleteUserButton;
