//@flow
import React from "react";
import type { User } from "../types/User";

type Props = {
  user: User,
  editUser: User => void
};

type State = {};

class EditUserButton extends React.Component<Props, State> {
  render() {
    if (!this.isEditable()) {
      return "";
    }
    return (
      <button type="button" onClick={e => this.props.editUser(this.props.user)}>
        Edit user
      </button>
    );
  }

  isEditable = () => {
    return this.props.user._links.update;
  };
}

export default EditUserButton;
