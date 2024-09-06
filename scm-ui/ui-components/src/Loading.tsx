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
import classNames from "classnames";
import styled from "styled-components";
import Image from "./Image";

type Props = WithTranslation & {
  message?: string;
};

const Wrapper = styled.div`
  min-height: 256px;
`;

const FixedSizedImage = styled(Image)`
  width: 128px;
  height: 128px;
`;

/**
 * @deprecated Please import the identical module from "@scm-manager/ui-core"
 */

class Loading extends React.Component<Props> {
  render() {
    const { message, t } = this.props;
    return (
      <Wrapper
        className={classNames(
          "is-flex",
          "is-flex-direction-column",
          "is-justify-content-center",
          "is-align-items-center"
        )}
      >
        <FixedSizedImage className="mb-3" src="/images/loading.svg" alt={t("loading.alt")} />
        <p className="has-text-centered">{message}</p>
      </Wrapper>
    );
  }
}

export default withTranslation("commons")(Loading);
