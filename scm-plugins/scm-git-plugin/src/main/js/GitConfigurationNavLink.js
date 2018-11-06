//@flow
import React from "react";
import { NavLink } from "@scm-manager/ui-components";
import { translate } from "react-i18next";

type Props = {
  url: string,

  // context props
  t: (string) => string
}
class GitConfigurationNavLink extends React.Component<Props> {

  render() {
    const { url, t } = this.props;
    return <NavLink to={`${url}/git`} label={t("scm-git-plugin.config.link")} />;
  }

}

export default translate("plugins")(GitConfigurationNavLink);
