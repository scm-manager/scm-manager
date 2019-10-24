import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Branch } from "@scm-manager/ui-types";

type Props = WithTranslation & {
  branch: Branch;
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

export default withTranslation("plugins")(HgBranchInformation);
