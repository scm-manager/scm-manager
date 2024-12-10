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

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { devices, ErrorNotification, Image, Loading, Subtitle, Title } from "@scm-manager/ui-components";
import { useUpdateInfo, useVersion } from "@scm-manager/ui-api";
import { useDocumentTitle } from "@scm-manager/ui-core";

const BoxShadowBox = styled.div`
  box-shadow: 0 2px 3px rgba(40, 177, 232, 0.1), 0 0 0 2px rgba(40, 177, 232, 0.2);
`;

const ImageWrapper = styled.div`
  padding: 0.2rem 0.4rem;
`;

const MobileWrapped = styled.article`
  @media screen and (max-width: ${devices.mobile.width}px) {
    flex-wrap: wrap;
    .button {
      height: 100%;
      word-break: break-word;
      white-space: break-spaces;
    }
  }
`;

const AdminDetails: FC = () => {
  const [t] = useTranslation("admin");
  useDocumentTitle(t("admin.info.title"));
  const version = useVersion();
  const { data: updateInfo, error, isLoading } = useUpdateInfo();

  if (isLoading) {
    return <Loading />;
  }

  if (error) {
    return <ErrorNotification error={error} />;
  }

  const renderUpdateInfo = () => (
    <>
      <BoxShadowBox className="box">
        <MobileWrapped className="media">
          <ImageWrapper className="media-left image is-96x96">
            <Image src="/images/blib.jpg" alt={t("admin.info.logo")} />
          </ImageWrapper>
          <div className="media-content">
            <div className="content">
              <h3 className="has-text-weight-medium">{t("admin.info.newRelease.title")}</h3>
              <p>
                {t("admin.info.newRelease.description", {
                  version: updateInfo?.latestVersion,
                })}
              </p>
              <a className="button is-warning is-pulled-right" target="_blank" href={updateInfo?.link} rel="noreferrer">
                {t("admin.info.newRelease.downloadButton")}
              </a>
            </div>
          </div>
        </MobileWrapped>
      </BoxShadowBox>
      <hr />
    </>
  );

  return (
    <>
      <Title title={t("admin.info.title")} />
      <Subtitle className="mb-1" subtitle={t("admin.info.currentAppVersion")} />
      <div className="mb-5">{version}</div>
      {updateInfo ? renderUpdateInfo() : null}
      <BoxShadowBox className="box">
        <MobileWrapped className="media">
          <ImageWrapper className="media-left">
            <Image src="/images/iconCommunitySupport.png" alt={t("admin.info.communityIconAlt")} />
          </ImageWrapper>
          <div className="media-content">
            <div className="content">
              <h3 className="has-text-weight-medium">{t("admin.info.communityTitle")}</h3>
              <p>{t("admin.info.communityInfo")}</p>
              <a
                className="button is-info is-pulled-right"
                target="_blank"
                href="https://scm-manager.org/support/"
                rel="noreferrer"
              >
                {t("admin.info.communityButton")}
              </a>
            </div>
          </div>
        </MobileWrapped>
      </BoxShadowBox>
      <BoxShadowBox className="box">
        <MobileWrapped className="media">
          <ImageWrapper className="media-left">
            <Image src="/images/iconEnterpriseSupport.png" alt={t("admin.info.enterpriseIconAlt")} />
          </ImageWrapper>
          <div className="media-content">
            <div className="content">
              <h3 className="has-text-weight-medium">{t("admin.info.enterpriseTitle")}</h3>
              <p>
                {t("admin.info.enterpriseInfo")}
                <br />
                <strong>{t("admin.info.enterprisePartner")}</strong>
              </p>
              <a
                className="button is-info is-pulled-right is-normal"
                target="_blank"
                href={t("admin.info.enterpriseLink")}
                rel="noreferrer"
              >
                {t("admin.info.enterpriseButton")}
              </a>
            </div>
          </div>
        </MobileWrapped>
      </BoxShadowBox>
    </>
  );
};

export default AdminDetails;
