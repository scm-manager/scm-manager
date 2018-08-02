//@flow
import React from 'react';
import NavLink from "../../../components/navigation/NavLink";
import { translate } from "react-i18next";
import type { Group } from "../../types/Group";

type Props = {
  t: string => string,
  editUrl: string,
  group: Group
}

type State = {
}

class EditGroupNavLink extends React.Component<Props, State> {

  render() {
    const { t, editUrl } = this.props;
    if (!this.isEditable()) {
      return null;
    }
    return <NavLink label={t("edit-group-button.label")} to={editUrl} />;
  }

  isEditable = () => {
    return this.props.group._links.update;
  }
}

export default translate("groups")(EditGroupNavLink);