import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import Image from "./Image";

class Logo extends React.Component<WithTranslation> {
  render() {
    const { t } = this.props;
    return <Image src="/images/logo.png" alt={t("logo.alt")} />;
  }
}

export default withTranslation("commons")(Logo);
