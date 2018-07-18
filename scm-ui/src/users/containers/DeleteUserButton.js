// @flow
import React from "react";
import type { User } from "../types/User";
import type { UserEntry } from "../types/UserEntry";
import { confirmAlert } from "../../components/ConfirmAlert";
import DeleteButton from "../../components/DeleteButton";

type Props = {
  entry: UserEntry,
  confirmDialog?: boolean,
  deleteUser: (user: User) => void
};

class DeleteUserButton extends React.Component<Props> {
  static defaultProps = {
    confirmDialog: true
  };

  deleteUser = () => {
    this.props.deleteUser(this.props.entry.entry);
  };

  confirmDelete = () => {
    confirmAlert({
      title: "Delete user",
      message: "Do you really want to delete the user?",
      buttons: [
        {
          label: "Yes",
          onClick: () => this.deleteUser()
        },
        {
          label: "No",
          onClick: () => null
        }
      ]
    });
  };

  isDeletable = () => {
    return this.props.entry.entry._links.delete;
  };

  render() {
    const { confirmDialog, entry } = this.props;
    const action = confirmDialog ? this.confirmDelete : this.deleteUser;

    if (!this.isDeletable()) {
      return;
    }
    return (
      <DeleteButton label="Delete" action={action} loading={entry.loading} />
    );
  }
}

export default DeleteUserButton;
