//@flow
import React from "react";
import { translate } from "react-i18next";
import { Title, GlobalConfiguration } from "@scm-manager/ui-components";
import GitConfigurationForm from "./GitConfigurationForm";

type Props = {
  link: string,

  t: (string) => string
};

class GitGlobalConfiguration extends React.Component<Props> {

  constructor(props: Props) {
    super(props);
  }

  render() {
    const { link, t } = this.props;

    return (
      <div>
        <Title title={t("scm-git-plugin.config.title")}/>
        <GlobalConfiguration link={link} render={props => <GitConfigurationForm {...props} />}/>
      </div>
    );
  }

}

export default translate("plugins")(GitGlobalConfiguration);
