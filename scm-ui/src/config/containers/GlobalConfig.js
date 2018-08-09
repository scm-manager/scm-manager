import React from "react";
import { Page } from "../../components/layout";
import type { History } from "history";
import { translate } from "react-i18next";

type Props = {
  // context objects
  t: string => string
};

class GlobalConfig extends React.Component<Props> {
  render() {
    const { t } = this.props;

    return <div>Here, global config will be shown</div>;
  }
}

export default translate("config")(GlobalConfig);
