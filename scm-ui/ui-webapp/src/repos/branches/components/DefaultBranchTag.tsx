import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import styled from "styled-components";
import { Tag } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  defaultBranch?: boolean;
};

const LeftMarginTag = styled(Tag)`
  vertical-align: inherit;
  margin-left: 0.75rem;
`;

class DefaultBranchTag extends React.Component<Props> {
  render() {
    const { defaultBranch, t } = this.props;

    if (defaultBranch) {
      return <LeftMarginTag color="dark" label={t("branch.defaultTag")} />;
    }
    return null;
  }
}

export default withTranslation("repos")(DefaultBranchTag);
