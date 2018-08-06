//@flow
import React from "react";
import { NavLink } from "../../components/navigation";
import { translate } from "react-i18next";
import type { Repository } from "../types/Repositories";

type Props = { editUrl: string, t: string => string, repository: Repository };

class EditNavLink extends React.Component<Props> {
  isEditable = () => {
    return this.props.repository._links.update;
  };
  render() {
    if (!this.isEditable()) {
      return null;
    }
    const { editUrl, t } = this.props;
    return <NavLink to={editUrl} label={t("edit-nav-link.label")} />;
  }
}

export default translate("repos")(EditNavLink);
