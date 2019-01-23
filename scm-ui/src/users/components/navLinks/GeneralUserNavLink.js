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

class GeneralUserNavLink extends React.Component<Props> {
  render() {
    const { t, editUrl } = this.props;

    if (!this.isEditable()) {
      return null;
    }
    return <NavLink label={t("single-user.menu.editNavLink")} to={editUrl} />;
  }

  isEditable = () => {
    return this.props.user._links.update;
  };
}

export default translate("users")(GeneralUserNavLink);
