// @flow
import React from "react";

type Props = {
  user: any,
  deleteUser: (link: string) => void
};

class DeleteUser extends React.Component<Props> {

  deleteUser = () => {
    this.props.deleteUser(this.props.user._links.delete.href);
  };

  if(deleteButtonClicked) {
    let deleteButtonAsk = <div>You really want to remove this user?</div>
  }

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
