import React from "react";
import { translate } from "react-i18next";
import Title from "../../components/layout/Title";

type Props = {
  // context objects
  t: string => string
};

class GlobalConfig extends React.Component<Props> {
  render() {
    const { t } = this.props;

    return <Title title={t("config.title")} />;
  }
}

export default translate("config")(GlobalConfig);
