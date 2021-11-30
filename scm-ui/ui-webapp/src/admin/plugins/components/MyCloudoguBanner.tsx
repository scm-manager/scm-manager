import { Button, Image } from "@scm-manager/ui-components";
import * as React from "react";
import styled from "styled-components";
import { useTranslation } from "react-i18next";
import { usePluginCenterLogin } from "@scm-manager/ui-api";

const MyCloudoguBannerWrapper = styled.div`
  border: 1px solid #123;
`;

const TextImage = styled(Image)`
  padding-top: 3px;
  height: 1.25em;
`;

const MyCloudoguBanner = () => {
  const login = usePluginCenterLogin();
  const [t] = useTranslation("admin");
  return login ? (
    <MyCloudoguBannerWrapper className="has-rounded-border is-flex is-flex-direction-column is-align-items-center p-5 mb-4">
      <Button className="mb-5" reducedMobile={true} label={t("plugins.myCloudogu.login.button.label")} link={login}>
        <TextImage alt="myCloudogu" src="/images/myCloudogu.svg" className="ml-1" />
      </Button>
      <p className="is-align-self-flex-start is-size-7">{t("plugins.myCloudogu.login.description")}</p>
    </MyCloudoguBannerWrapper>
  ) : null;
};

export default MyCloudoguBanner;
