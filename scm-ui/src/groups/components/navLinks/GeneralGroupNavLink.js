//@flow
import React from "react";
import { NavLink } from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import type { Group } from "@scm-manager/ui-types";

type Props = {
  t: string => string,
  editUrl: string,
  group: Group
};

type State = {};

class GeneralGroupNavLink extends React.Component<Props, State> {
  render() {
    const { t, editUrl } = this.props;
    if (!this.isEditable()) {
      return null;
    }
    return <NavLink label={t("singleGroup.menu.generalNavLink")} to={editUrl} />;
  }

  isEditable = () => {
    return this.props.group._links.update;
  };
}

export default translate("groups")(GeneralGroupNavLink);
