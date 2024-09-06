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

import * as React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { InfoItem, Link } from "@scm-manager/ui-types";
import { devices, Icon } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  type: "plugin" | "feature";
  item?: InfoItem;
};

const FixedSizedIconWrapper = styled.div`
  width: 160px;
  height: 160px;
  @media screen and (max-width: ${devices.mobile.width}px) {
    margin-bottom: 1rem;
  }
`;

const ContentWrapper = styled.div`
  min-height: 10.5rem;
`;

const InfoBoxWrapper = styled.div`
  @media screen and (max-width: ${devices.mobile.width}px) {
    flex-wrap: wrap;
    justify-content: center;
  }
`;

class InfoBox extends React.Component<Props> {
  renderBody = () => {
    const { item, t } = this.props;
    const title = item ? item.title : t("login.loading");
    const summary = item ? item.summary : t("login.loading");

    return (
      <ContentWrapper className={classNames("media-content", "content", "ml-5")}>
        <h4 className="has-text-link">{title}</h4>
        <p>{summary}</p>
      </ContentWrapper>
    );
  };

  render() {
    const { item, type, t } = this.props;
    const icon = type === "plugin" ? "puzzle-piece" : "star";
    return (
      <a className="is-block mb-5" href={(item?._links?.self as Link)?.href}>
        <InfoBoxWrapper className="box media">
          <figure className="media-left">
            <FixedSizedIconWrapper
              className={classNames(
                "image",
                "box",
                "has-text-weight-bold",
                "has-text-secondary-least",
                "has-background-info",
                "is-flex",
                "is-flex-direction-column",
                "is-justify-content-center",
                "is-align-items-center"
              )}
            >
              <Icon className="mb-2 fa-2x" name={icon} color="white" alt="" />
              <div className="is-size-4 has-text-white">{t("login." + type)}</div>
              <div className="is-size-4 has-text-white">{t("login.tip")}</div>
            </FixedSizedIconWrapper>
          </figure>
          {this.renderBody()}
        </InfoBoxWrapper>
      </a>
    );
  }
}

export default withTranslation("commons")(InfoBox);
