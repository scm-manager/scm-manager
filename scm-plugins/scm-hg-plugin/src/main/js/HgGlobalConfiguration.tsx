import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Title, Configuration } from "@scm-manager/ui-components";
import HgConfigurationForm from "./HgConfigurationForm";

type Props = WithTranslation & {
  link: string;
};

class HgGlobalConfiguration extends React.Component<Props> {
  render() {
    const { link, t } = this.props;
    return (
      <div>
        <Title title={t("scm-hg-plugin.config.title")} />
        <Configuration link={link} render={(props: any) => <HgConfigurationForm {...props} />} />
      </div>
    );
  }
}

export default withTranslation("plugins")(HgGlobalConfiguration);
