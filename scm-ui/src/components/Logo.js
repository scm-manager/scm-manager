//@flow
import React from "react";
import { translate } from "react-i18next";

type Props = {
  t: string => string
};

class Logo extends React.Component<Props> {
  render() {
    const { t } = this.props;
    return <img src="images/logo.png" alt={t("logo.alt")} />;
  }
}

export default translate("commons")(Logo);
