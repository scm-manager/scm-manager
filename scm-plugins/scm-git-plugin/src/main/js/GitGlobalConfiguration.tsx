import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Title, Configuration } from "@scm-manager/ui-components";
import GitConfigurationForm from "./GitConfigurationForm";

type Props = WithTranslation & {
  link: string;
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

export default withTranslation("plugins")(GitGlobalConfiguration);
