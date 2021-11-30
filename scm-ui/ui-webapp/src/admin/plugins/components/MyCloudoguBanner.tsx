import { Button, Image } from "@scm-manager/ui-components";
import * as React from "react";
import styled from "styled-components";
import { Trans, useTranslation } from "react-i18next";
import { usePluginCenterLogin } from "@scm-manager/ui-api";

const MyCloudoguBannerWrapper = styled.div`
  border: 1px solid;
`;

const MyCloudoguBanner = () => {
  const login = usePluginCenterLogin();
  const [t] = useTranslation("admin");
  return login ? (
    <MyCloudoguBannerWrapper className="has-rounded-border is-flex is-flex-direction-column is-align-items-center p-5 mb-4 has-border-success">
      <Button className="mb-5 has-text-weight-normal has-border-info" reducedMobile={true} link={login}>
        <Trans
          t={t}
          i18nKey="plugins.myCloudogu.login.button.label"
          components={[<span className="mx-1 has-text-info">myCloudogu</span>]}
        />
      </Button>
      <p className="is-align-self-flex-start is-size-7">
        <Trans
          t={t}
          i18nKey="plugins.myCloudogu.login.description"
          components={[<a href="https://my.cloudogu.com/">myCloudogu</a>]}
        />
      </p>
    </MyCloudoguBannerWrapper>
  ) : null;
};

export default MyCloudoguBanner;
