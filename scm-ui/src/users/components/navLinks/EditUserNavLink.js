//@flow
import React from "react";
import { translate } from "react-i18next";
import type { User } from "../../types/User";
import { NavLink } from "../../../components/navigation";

type Props = {
  t: string => string,
  user: User,
  editUrl: String
};

class EditUserNavLink extends React.Component<Props> {
  render() {
    const { t, editUrl } = this.props;

    if (!this.isEditable()) {
      return null;
    }
    return <NavLink label={t("edit-user-button.label")} to={editUrl} />;
  }

  isEditable = () => {
    return this.props.user._links.update;
  };
}

export default translate("users")(EditUserNavLink);
