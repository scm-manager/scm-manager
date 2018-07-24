//@flow
import React from "react";
import EditButton from "../../components/EditButton";
import type { UserEntry } from "../types/UserEntry";

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
    return <EditButton label="Edit" link={link} loading={entry.loading} />;
  }

  isEditable = () => {
    return this.props.entry.entry._links.update;
  };
}

export default EditUserButton;
