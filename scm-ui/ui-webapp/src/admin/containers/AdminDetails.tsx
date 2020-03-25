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
import React from "react";
import { connect } from "react-redux";
import { WithTranslation, withTranslation } from "react-i18next";
import styled from "styled-components";
import { Image, Loading, Subtitle, Title } from "@scm-manager/ui-components";
import { getAppVersion } from "../../modules/indexResource";

type Props = WithTranslation & {
  loading: boolean;
  error: Error;
  version: string;
};

const NoBottomMarginSubtitle = styled(Subtitle)`
  margin-bottom: 0.25rem !important;
`;

const BottomMarginDiv = styled.div`
  margin-bottom: 1.5rem;
`;

const BoxShadowBox = styled.div`
  box-shadow: 0 2px 3px rgba(40, 177, 232, 0.1), 0 0 0 2px rgba(40, 177, 232, 0.2);
`;

const ImageWrapper = styled.div`
  padding: 0.2rem 0.4rem;
`;

class AdminDetails extends React.Component<Props> {
  render() {
    const { loading, t } = this.props;

    if (loading) {
      return <Loading />;
    }

    return (
      <>
        <Title title={t("admin.info.title")} />
        <NoBottomMarginSubtitle subtitle={t("admin.info.currentAppVersion")} />
        <BottomMarginDiv>{this.props.version}</BottomMarginDiv>
        <BoxShadowBox className="box">
          <article className="media">
            <ImageWrapper className="media-left">
              <Image src="/images/iconCommunitySupport.png" alt={t("admin.info.communityIconAlt")} />
            </ImageWrapper>
            <div className="media-content">
              <div className="content">
                <h3 className="has-text-weight-medium">{t("admin.info.communityTitle")}</h3>
                <p>{t("admin.info.communityInfo")}</p>
                <a className="button is-info is-pulled-right" target="_blank" href="https://scm-manager.org/support/">
                  {t("admin.info.communityButton")}
                </a>
              </div>
            </div>
          </article>
        </BoxShadowBox>
        <BoxShadowBox className="box">
          <article className="media">
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
                >
                  {t("admin.info.enterpriseButton")}
                </a>
              </div>
            </div>
          </article>
        </BoxShadowBox>
      </>
    );
  }
}

const mapStateToProps = (state: any) => {
  const version = getAppVersion(state);
  return {
    version
  };
};

export default connect(mapStateToProps)(withTranslation("admin")(AdminDetails));
