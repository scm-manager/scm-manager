import React from "react";
import { Group } from "@scm-manager/ui-types";
import { NavLink } from "@scm-manager/ui-components";
import { WithTranslation, withTranslation } from "react-i18next";

type Props = WithTranslation & {
  group: Group;
  editUrl: string;
};

class EditGroupNavLink extends React.Component<Props> {
  isEditable = () => {
    return this.props.group._links.update;
  };

  render() {
    const { t, editUrl } = this.props;

    if (!this.isEditable()) {
      return null;
    }
    return <NavLink to={editUrl} label={t("singleGroup.menu.generalNavLink")} />;
  }
}

export default withTranslation("groups")(EditGroupNavLink);
