//@flow
import React from "react";
import EditButton from "../../components/EditButton";
import type { User } from "../types/User";
import type { UserEntry } from "../types/UserEntry";
import { Link } from "react-router-dom";

type Props = {
  entry: UserEntry
};

class EditUserButton extends React.Component<Props> {
  render() {
    const { entry } = this.props;
    const link = "/users/edit/" + entry.entry.name;

    if (!this.isEditable()) {
      return "";
    }
    return (
      <Link to={link}>
        <EditButton label="Edit" action={() => {}} loading={entry.loading} />
      </Link>
    );
  }

  isEditable = () => {
    return this.props.entry.entry._links.update;
  };
}

export default EditUserButton;
