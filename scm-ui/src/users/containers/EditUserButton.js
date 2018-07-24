//@flow
import React from "react";
import { translate } from "react-i18next";
import EditButton from "../../components/EditButton";
import type { UserEntry } from "../types/UserEntry";

type Props = {
  t: string => string,
  entry: UserEntry
};

class EditUserButton extends React.Component<Props> {
  render() {
    const { entry, t } = this.props;
    const link = "/users/edit/" + entry.entry.name;

    if (!this.isEditable()) {
      return "";
    }
    return (
      <EditButton
        label={t("edit-user-button.label")}
        link={link}
        loading={entry.loading}
      />
    );
  }

  isEditable = () => {
    return this.props.entry.entry._links.update;
  };
}

export default translate("users")(EditUserButton);
