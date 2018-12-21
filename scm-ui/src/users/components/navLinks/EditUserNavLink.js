//@flow
import React from "react";
import { translate } from "react-i18next";
import type { User } from "@scm-manager/ui-types";
import { NavLink } from "@scm-manager/ui-components";

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
    return <NavLink to={editUrl} icon="fas fa-cog" label={t("edit-user-button.label")} />;
  }

  isEditable = () => {
    return this.props.user._links.update;
  };
}

export default translate("users")(EditUserNavLink);
