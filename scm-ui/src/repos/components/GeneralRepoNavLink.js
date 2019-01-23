//@flow
import React from "react";
import { NavLink } from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import type { Repository } from "@scm-manager/ui-types";

type Props = { editUrl: string, t: string => string, repository: Repository };

class GeneralRepoNavLink extends React.Component<Props> {
  isEditable = () => {
    return this.props.repository._links.update;
  };
  render() {
    if (!this.isEditable()) {
      return null;
    }
    const { editUrl, t } = this.props;
    return <NavLink to={editUrl} label={t("repositoryRoot.menu.generalNavLink")} />;
  }
}

export default translate("repos")(GeneralRepoNavLink);
