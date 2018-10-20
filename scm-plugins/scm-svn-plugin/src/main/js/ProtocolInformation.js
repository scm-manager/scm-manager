//@flow
import React from "react";
import { repositories } from "@scm-manager/ui-components";
import type { Repository } from "@scm-manager/ui-types";
import { translate } from "react-i18next";

type Props = {
  repository: Repository,
  t: string => string
}

class ProtocolInformation extends React.Component<Props> {

  render() {
    const { repository } = this.props;
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

export default translate("plugins")(ProtocolInformation);
