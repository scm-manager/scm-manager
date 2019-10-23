import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Title, Configuration } from "@scm-manager/ui-components";
import SvnConfigurationForm from "./SvnConfigurationForm";

type Props = WithTranslation & {
  link: string;
};

class SvnGlobalConfiguration extends React.Component<Props> {
  render() {
    const { link, t } = this.props;
    return (
      <div>
        <Title title={t("scm-svn-plugin.config.title")} />
        <Configuration link={link} render={(props: any) => <SvnConfigurationForm {...props} />} />
      </div>
    );
  }
}

export default withTranslation("plugins")(SvnGlobalConfiguration);
