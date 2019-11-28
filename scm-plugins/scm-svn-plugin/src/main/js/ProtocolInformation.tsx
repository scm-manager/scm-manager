import React from "react";
import { withTranslation, WithTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { repositories } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  repository: Repository;
};

class ProtocolInformation extends React.Component<Props> {
  render() {
    const { repository, t } = this.props;
    const href = repositories.getProtocolLinkByType(repository, "http");
    if (!href) {
      return null;
    }
    return (
      <div>
        <h4>{t("scm-svn-plugin.information.checkout")}</h4>
        <pre>
          <code>svn checkout {href}</code>
        </pre>
      </div>
    );
  }
}

export default withTranslation("plugins")(ProtocolInformation);
