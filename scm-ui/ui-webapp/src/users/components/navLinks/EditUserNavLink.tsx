import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { User } from "@scm-manager/ui-types";
import { NavLink } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  user: User;
  editUrl: string;
};

class EditUserNavLink extends React.Component<Props> {
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

export default withTranslation("users")(EditUserNavLink);
