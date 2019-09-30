//@flow
import React from "react";
import type { Changeset } from "@scm-manager/ui-types";
import LoadingDiff from "../LoadingDiff";
import Notification from "../../Notification";
import {translate} from "react-i18next";

type Props = {
  changeset: Changeset,
  defaultCollapse?: boolean,

  // context props
  t: string => string
};

class ChangesetDiff extends React.Component<Props> {

  isDiffSupported(changeset: Changeset) {
    return !!changeset._links.diff;
  }

  createUrl(changeset: Changeset) {
    return changeset._links.diff.href + "?format=GIT";
  }

  render() {
    const { changeset, defaultCollapse, t } = this.props;
    if (!this.isDiffSupported(changeset)) {
      return <Notification type="danger">{t("changeset.diffNotSupported")}</Notification>;
    } else {
      const url = this.createUrl(changeset);
      return <LoadingDiff url={url} defaultCollapse={defaultCollapse} />;
    }
  }

}

export default translate("repos")(ChangesetDiff);
