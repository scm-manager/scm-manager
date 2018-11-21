//@flow
import React from "react";
import { translate } from "react-i18next";
import { Title, Configuration } from "@scm-manager/ui-components";
import SvnConfigurationForm from "./SvnConfigurationForm";

type Props = {
  link: string,

  // context props
  t: (string) => string
}

class SvnGlobalConfiguration extends React.Component<Props> {

  render() {
    const  { link, t } = this.props;
    return (
      <div>
        <Title title={t("scm-svn-plugin.config.title")}/>
        <Configuration link={link} render={props => <SvnConfigurationForm {...props} />}/>
      </div>
    );
  }

}

export default translate("plugins")(SvnGlobalConfiguration);
