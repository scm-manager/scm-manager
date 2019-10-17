//@flow
import React from "react";
import { translate } from "react-i18next";
import styled from "styled-components";
import { Tag } from "@scm-manager/ui-components";

type Props = {
  system?: boolean,

  // context props
  t: string => string
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

export default translate("admin")(SystemRoleTag);
