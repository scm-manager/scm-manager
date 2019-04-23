//@flow
import React from "react";
import { translate } from "react-i18next";
import { CreateButton } from "@scm-manager/ui-components";

type Props = {
  t: string => string
};

class CreateUserButton extends React.Component<Props> {
  render() {
    const { t } = this.props;
    return (
      <CreateButton label={t("users.createButton")} link="/users/add" />
    );
  }
}

export default translate("users")(CreateUserButton);
