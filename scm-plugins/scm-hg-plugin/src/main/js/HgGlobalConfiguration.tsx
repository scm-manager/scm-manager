import React from "react";
import { Title, Configuration } from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import HgConfigurationForm from "./HgConfigurationForm";

type Props = {
  link: string;

  // context props
  t: (p: string) => string;
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

export default translate("plugins")(HgGlobalConfiguration);
