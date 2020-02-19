import React, { FC } from "react";
import { Me, Links } from "@scm-manager/ui-types";
import { useBinder, ExtensionPoint } from "@scm-manager/ui-extensions";
import { AvatarImage } from "../avatar";
import NavLink from "../navigation/NavLink";
import FooterSection from "./FooterSection";
import styled from "styled-components";
import { EXTENSION_POINT } from "../avatar/Avatar";

type Props = {
  me?: Me;
  version: string;
  links: Links;
};

type TitleWithIconsProps = {
  title: string;
  icon: string;
};

const TitleWithIcon: FC<TitleWithIconsProps> = ({ icon, title }) => (
  <>
    <i className={`fas fa-${icon} fa-fw`} /> {title}
  </>
);

type TitleWithAvatarProps = {
  me: Me;
};

const VCenteredAvatar = styled(AvatarImage)`
  vertical-align: middle;
`;

const AvatarContainer = styled.span`
  float: left;
  margin-right: 0.3em;
  padding-top: 0.2em;
  width: 1em;
  height: 1em;
`;

const TitleWithAvatar: FC<TitleWithAvatarProps> = ({ me }) => (
  <>
    <AvatarContainer className="image is-rounded">
      <VCenteredAvatar person={me} representation="rounded" />
    </AvatarContainer>
    {me.displayName}
  </>
);

const Footer: FC<Props> = ({ me, version, links }) => {
  const binder = useBinder();
  if (!me) {
    return null;
  }
  const extensionProps = { me, url: "/me", links };
  let meSectionTile;
  if (binder.hasExtension(EXTENSION_POINT)) {
    meSectionTile = <TitleWithAvatar me={me} />;
  } else {
    meSectionTile = <TitleWithIcon title={me.displayName} icon="user-circle" />;
  }

  return (
    <footer className="footer">
      <section className="section container">
        <div className="columns is-size-7">
          <FooterSection title={meSectionTile}>
            <NavLink to="#" label="Profile" />
            <NavLink to="#" label="Change Password" />
            <ExtensionPoint name="profile.setting" props={extensionProps} renderAll={true} />
          </FooterSection>
          <FooterSection title={<TitleWithIcon title="Information" icon="info-circle" />}>
            <a href="https://www.scm-manager.org/" target="_blank">
              SCM-Manager {version}
            </a>
            <ExtensionPoint name="footer.links" props={extensionProps} renderAll={true} />
          </FooterSection>
          <FooterSection title={<TitleWithIcon title="About" icon="external-link-alt" />}>
            <a href="https://www.scm-manager.org/" target="_blank">
              Learn more
            </a>
            <a target="_blank" href="https://cloudogu.com/">
              Powered by Cloudogu
            </a>
            <ExtensionPoint name="footer.links" props={extensionProps} renderAll={true} />
          </FooterSection>
        </div>
      </section>
    </footer>
  );
};

export default Footer;
