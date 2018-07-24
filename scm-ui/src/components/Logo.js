//@flow
import React from "react";
import { translate } from "react-i18next";
import Image from "../images/logo.png";

type Props = {
  t: string => string
};

class Logo extends React.Component<Props> {
  render() {
    const { t } = this.props;
    return <img src={Image} alt={t("logo.alt")} />;
  }
}

export default translate("commons")(Logo);
