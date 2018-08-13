// @flow
import React from "react";
import { translate } from "react-i18next";
import { Checkbox, InputField } from "../../../components/forms/index";
import Subtitle from "../../../components/layout/Subtitle";

type Props = {
  adminGroups: string[],
  adminUsers: string[],
  t: string => string,
  onChange: (boolean, any, string) => void
};
//TODO: Einbauen!
class AdminSettings extends React.Component<Props> {
  render() {
    const {
      t,
      adminGroups,
      adminUsers
    } = this.props;

    return (
      null
    );
  }

}

export default translate("config")(AdminSettings);
