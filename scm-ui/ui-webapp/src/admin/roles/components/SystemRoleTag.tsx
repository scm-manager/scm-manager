import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import styled from "styled-components";
import { Tag } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  system?: boolean;
};

const LeftMarginTag = styled(Tag)`
  margin-left: 0.75rem;
  vertical-align: inherit;
`;

class SystemRoleTag extends React.Component<Props> {
  render() {
    const { system, t } = this.props;

    if (system) {
      return <LeftMarginTag color="dark" label={t("repositoryRole.system")} />;
    }
    return null;
  }
}

export default withTranslation("admin")(SystemRoleTag);
