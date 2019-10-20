import React from "react";
import { translate } from "react-i18next";
import Image from "./Image";

type Props = {
  t: (p: string) => string;
};

class Logo extends React.Component<Props> {
  render() {
    const { t } = this.props;
    return <Image src="/images/logo.png" alt={t("logo.alt")} />;
  }
}

export default translate("commons")(Logo);
