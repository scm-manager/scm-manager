import React from "react";
import { Branch } from "@scm-manager/ui-types";
import { translate } from "react-i18next";

type Props = {
  branch: Branch;
  t: (p: string) => string;
};

class GitBranchInformation extends React.Component<Props> {
  render() {
    const { branch, t } = this.props;

    return (
      <div>
        <h4>{t("scm-git-plugin.information.fetch")}</h4>
        <pre>
          <code>git fetch</code>
        </pre>
        <h4>{t("scm-git-plugin.information.checkout")}</h4>
        <pre>
          <code>git checkout {branch.name}</code>
        </pre>
      </div>
    );
  }
}

export default translate("plugins")(GitBranchInformation);
