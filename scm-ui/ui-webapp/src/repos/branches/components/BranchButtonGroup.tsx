import React from "react";
import { Repository, Branch } from "@scm-manager/ui-types";
import { ButtonAddons, Button } from "@scm-manager/ui-components";
import { translate } from "react-i18next";

type Props = {
  repository: Repository;
  branch: Branch;

  // context props
  t: (p: string) => string;
};

class BranchButtonGroup extends React.Component<Props> {
  render() {
    const { repository, branch, t } = this.props;

    const changesetLink = `/repo/${repository.namespace}/${
      repository.name
    }/branch/${encodeURIComponent(branch.name)}/changesets/`;
    const sourcesLink = `/repo/${repository.namespace}/${
      repository.name
    }/sources/${encodeURIComponent(branch.name)}/`;

    return (
      <ButtonAddons>
        <Button
          link={changesetLink}
          icon="exchange-alt"
          label={t("branch.commits")}
          reducedMobile={true}
        />
        <Button
          link={sourcesLink}
          icon="code"
          label={t("branch.sources")}
          reducedMobile={true}
        />
      </ButtonAddons>
    );
  }
}

export default translate("repos")(BranchButtonGroup);
