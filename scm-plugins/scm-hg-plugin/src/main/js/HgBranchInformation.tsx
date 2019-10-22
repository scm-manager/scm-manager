import React from "react";
import { Branch } from "@scm-manager/ui-types";
import { translate } from "react-i18next";

type Props = {
  branch: Branch;
  t: (p: string) => string;
};

class HgBranchInformation extends React.Component<Props> {
  render() {
    const { branch, t } = this.props;

    return (
      <div>
        <h4>{t("scm-hg-plugin.information.fetch")}</h4>
        <pre>
          <code>hg pull</code>
        </pre>
        <h4>{t("scm-hg-plugin.information.checkout")}</h4>
        <pre>
          <code>hg update {branch.name}</code>
        </pre>
      </div>
    );
  }
}

export default translate("plugins")(HgBranchInformation);
