//@flow
import React from "react";
import type { Repository } from "@scm-manager/ui-types";
import { translate } from "react-i18next";

type Props = {
  repository: Repository,
  t: string => string
};

class GitBranchInformation extends React.Component<Props> {
  render() {
    const { repository, t } = this.props;

    return (
      <div>
        <h4>{t("scm-git-plugin.information.fetch")}</h4>
        <pre>
          <code>git fetch</code>
        </pre>
        <h4>{t("scm-git-plugin.information.checkout")}</h4>
        <pre>
          <code>
            git checkout -placeholder-
            <br />
          </code>
        </pre>
      </div>
    ); // TODO: Placeholder
  }
}

export default translate("plugins")(GitBranchInformation);
