//@flow
import React from "react";
import type { User } from "@scm-manager/ui-types";
import { NavLink } from "@scm-manager/ui-components";
import { translate } from "react-i18next";

type Props = {
  user: User,
  editUrl: String,
  t: string => string
};

class GeneralUserNavLink extends React.Component<Props> {
  isEditable = () => {
    return this.props.user._links.update;
  };

  render() {
    const { t, editUrl } = this.props;

    if (!this.isEditable()) {
      return null;
    }
    return <NavLink to={editUrl} label={t("singleUser.menu.generalNavLink")} />;
  }
}

export default translate("users")(GeneralUserNavLink);
