//@flow
import React from "react";
import { translate } from "react-i18next";
import { CreateButton } from "../../../components/buttons";

type Props = {
  t: string => string
};

class CreateGroupButton extends React.Component<Props> {
  render() {
    const { t } = this.props;
    return (
      <CreateButton label={t("create-group-button.label")} link="/groups/add" />
    );
  }
}

export default translate("groups")(CreateGroupButton);
