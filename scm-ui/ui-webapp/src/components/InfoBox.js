//@flow
import * as React from "react";
import { translate } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import type { InfoItem } from "./InfoItem";
import { Icon } from "@scm-manager/ui-components";

type Props = {
  type: "plugin" | "feature",
  item: InfoItem,

  // context props
  t: string => string
};

const BottomMarginA = styled.a`
  display: blocK;
  margin-bottom: 1.5rem;
`;

const FixedSizedIconWrapper = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 160px;
  height: 160px;
`;

const LightBlueIcon = styled(Icon)`
  margin-bottom: 0.5em;
  color: #bff1e6;
`;

const ContentWrapper = styled.div`
  min-height: 1.5rem;
  margin-left: 1.5em;
`;

class InfoBox extends React.Component<Props> {
  renderBody = () => {
    const { item, t } = this.props;
    const title = item ? item.title : t("login.loading");
    const summary = item ? item.summary : t("login.loading");

    return (
      <ContentWrapper className={classNames("media-content", "content")}>
        <h4 className="has-text-link">{title}</h4>
        <p>{summary}</p>
      </ContentWrapper>
    );
  };

  render() {
    const { item, type, t } = this.props;
    const icon = type === "plugin" ? "puzzle-piece" : "star";
    return (
      <BottomMarginA href={item._links.self.href}>
        <div className="box media">
          <figure className="media-left">
            <FixedSizedIconWrapper
              className={classNames(
                "image",
                "box",
                "has-text-weight-bold",
                "has-text-white",
                "has-background-info"
              )}
            >
              <LightBlueIcon className="fa-2x" name={icon} color="inherit" />
              <div className="is-size-4">{t("login." + type)}</div>
              <div className="is-size-4">{t("login.tip")}</div>
            </FixedSizedIconWrapper>
          </figure>
          {this.renderBody()}
        </div>
      </BottomMarginA>
    );
  }
}

export default translate("commons")(InfoBox);
