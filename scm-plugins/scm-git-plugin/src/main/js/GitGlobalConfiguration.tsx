import React from "react";
import { translate } from "react-i18next";
import { Title, Configuration } from "@scm-manager/ui-components";
import GitConfigurationForm from "./GitConfigurationForm";

type Props = {
  link: string;

  t: (p: string) => string;
};

class GitGlobalConfiguration extends React.Component<Props> {
  render() {
    const { link, t } = this.props;

    return (
      <div>
        <Title title={t("scm-git-plugin.config.title")} />
        <Configuration link={link} render={(props: any) => <GitConfigurationForm {...props} />} />
      </div>
    );
  }
}

export default translate("plugins")(GitGlobalConfiguration);
