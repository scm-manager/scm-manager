import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { NavLink } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  repository: Repository;
  editUrl: string;
};

class EditRepoNavLink extends React.Component<Props> {
  isEditable = () => {
    return this.props.repository._links.update;
  };

  render() {
    const { editUrl, t } = this.props;

    if (!this.isEditable()) {
      return null;
    }
    return <NavLink to={editUrl} label={t("repositoryRoot.menu.generalNavLink")} />;
  }
}

export default withTranslation("repos")(EditRepoNavLink);
