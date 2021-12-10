/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import * as React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { InfoItem } from "@scm-manager/ui-types";
import { Icon } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  type: "plugin" | "feature";
  item?: InfoItem;
};

const FixedSizedIconWrapper = styled.div`
  width: 160px;
  height: 160px;
`;

const ContentWrapper = styled.div`
  min-height: 10.5rem;
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
      <a className="is-block mb-5" href={item._links.self.href}>
        <div className="box media">
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
              <Icon className="has-text-blue-light mb-2 fa-2x" name={icon} color="inherit" alt="" />
              <div className="is-size-4">{t("login." + type)}</div>
              <div className="is-size-4">{t("login.tip")}</div>
            </FixedSizedIconWrapper>
          </figure>
          {this.renderBody()}
        </div>
      </a>
    );
  }
}

export default withTranslation("commons")(InfoBox);
