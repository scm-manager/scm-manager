import React, { ReactNode } from "react";
import classNames from "classnames";
import styled from "styled-components";
import { Link } from "react-router-dom";

type Props = {
  title: string;
  description: string;
  avatar: ReactNode;
  contentRight?: ReactNode;
  footerLeft: ReactNode;
  footerRight: ReactNode;
  link?: string;
  action?: () => void;
  className?: string;
};

const NoEventWrapper = styled.article`
  position: relative;
  pointer-events: none;
  z-index: 1;
`;

const AvatarWrapper = styled.figure`
  margin-top: 0.8em;
  margin-left: 1em !important;
`;

const FlexFullHeight = styled.div`
  flex-direction: column;
  justify-content: space-around;
  align-self: stretch;
`;

const FooterWrapper = styled.div`
  padding-bottom: 1rem;
`;

const ContentLeft = styled.div`
  margin-bottom: 0 !important;
  overflow: hidden;
`;

const ContentRight = styled.div`
  margin-left: auto;
`;

export default class CardColumn extends React.Component<Props> {
  createLink = () => {
    const { link, action } = this.props;
    if (link) {
      return <Link className="overlay-column" to={link} />;
    } else if (action) {
      return (
        <a
          className="overlay-column"
          onClick={e => {
            e.preventDefault();
            action();
          }}
          href="#"
        />
      );
    }
    return null;
  };

  render() {
    const {
      avatar,
      title,
      description,
      contentRight,
      footerLeft,
      footerRight,
      className
    } = this.props;
    const link = this.createLink();
    return (
      <>
        {link}
        <NoEventWrapper className={classNames("media", className)}>
          <AvatarWrapper className="media-left">{avatar}</AvatarWrapper>
          <FlexFullHeight
            className={classNames("media-content", "text-box", "is-flex")}
          >
            <div className="is-flex">
              <ContentLeft className="content">
                <p className="shorten-text is-marginless">
                  <strong>{title}</strong>
                </p>
                <p className="shorten-text">{description}</p>
              </ContentLeft>
              <ContentRight>{contentRight}</ContentRight>
            </div>
            <FooterWrapper className={classNames("level", "is-flex")}>
              <div className="level-left is-hidden-mobile">{footerLeft}</div>
              <div className="level-right is-mobile is-marginless">
                {footerRight}
              </div>
            </FooterWrapper>
          </FlexFullHeight>
        </NoEventWrapper>
      </>
    );
  }
}
