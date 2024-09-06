/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Tag } from "@scm-manager/ui-components";
import styled from "styled-components";

type Props = WithTranslation & {
  defaultBranch?: boolean;
  className?: string;
};

const DefaultTag = styled(Tag)`
  max-height: 1.5em;
`;

class DefaultBranchTag extends React.Component<Props> {
  render() {
    const { defaultBranch, className, t } = this.props;

    if (defaultBranch) {
      return (
        <DefaultTag className={"is-unselectable " + (className || "")} color="dark" label={t("branch.defaultTag")} />
      );
    }
    return null;
  }
}

export default withTranslation("repos")(DefaultBranchTag);
