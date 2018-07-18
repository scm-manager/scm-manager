//@flow
import React from "react";
import EditButton from "../../components/EditButton";
import type { User } from "../types/User";

type Props = {
  entry: UserEntry,
  editUser: User => void
};

class EditUserButton extends React.Component<Props> {
  render() {
    if (!this.isEditable()) {
      return "";
    }
    const { entry, editUser } = this.props;
    return (
      <EditButton
        label="Edit"
        action={e => editUser(entry.entry)}
        loading={entry.loading}
      />
    );
  }

  isEditable = () => {
    return this.props.entry.entry._links.update;
  };
}

export default EditUserButton;
