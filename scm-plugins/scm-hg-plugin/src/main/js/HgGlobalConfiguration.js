//@flow
import React from "react";
import { Title, GlobalConfiguration } from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import HgConfigurationForm from "./HgConfigurationForm";

type Props = {
  link: string,

  // context props
  t: (string) => string
}

class HgGlobalConfiguration extends React.Component<Props> {

  render() {
    const  { link, t } = this.props;
    return (
      <div>
        <Title title={t("scm-hg-plugin.config.title")}/>
        <GlobalConfiguration link={link} render={props => <HgConfigurationForm {...props} />}/>
      </div>
    );
  }

}

export default translate("plugins")(HgGlobalConfiguration);
