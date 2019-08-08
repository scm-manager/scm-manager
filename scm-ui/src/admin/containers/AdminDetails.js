// @flow
import React from "react";
import {connect} from "react-redux";
import injectSheet from "react-jss";
import {translate} from "react-i18next";
import classNames from "classnames";
import {Image, Loading, Subtitle, Title} from "@scm-manager/ui-components";
import {getAppVersion} from "../../modules/indexResource";

type Props = {
  loading: boolean,
  error: Error,

  version: string,

  // context props
  classes: any,
  t: string => string
};

const styles = {
  boxShadow: {
    boxShadow: "0 2px 3px rgba(40, 177, 232, 0.1), 0 0 0 2px rgb(40, 177, 232, 0.2)"
  },
  boxTitle: {
    fontWeight: "500 !important"
  }
};

class AdminDetails extends React.Component<Props> {
  render() {
    const { loading, classes, t } = this.props;

    if (loading) {
      return <Loading />;
    }

    return (
      <>
        <Title title={t("admin.info.currentAppVersion")} />
        <Subtitle subtitle={this.props.version} />
        <div className={classNames("box", classes.boxShadow)}>
          <article className="media">
            <div className="media-left">
              <figure className="image is-96x96">
                <Image
                  src="/images/iconCommunitySupport.png"
                  alt={t("admin.info.communityIconAlt")}
                />
              </figure>
            </div>
            <div className="media-content">
              <div className="content">
                <h3 className={classes.boxTitle}>{t("admin.info.communityTitle")}</h3>
                <p>{t("admin.info.communityInfo")}</p>
                <a className="button is-info is-pulled-right" target="_blank" href="https://scm-manager.org/support/">{t("admin.info.communityButton")}</a>
              </div>
            </div>
          </article>
        </div>
        <div className={classNames("box", classes.boxShadow)}>
          <article className="media">
            <div className="media-left">
              <figure className="image is-96x96">
                <Image
                  src="/images/iconEnterpriseSupport.png"
                  alt={t("admin.info.enterpriseIconAlt")}
                />
              </figure>
            </div>
            <div className="media-content">
              <div className="content">
                <h3 className={classes.boxTitle}>{t("admin.info.enterpriseTitle")}</h3>
                <p>{t("admin.info.enterpriseInfo")}<br /><strong>{t("admin.info.enterprisePartner")}</strong></p>
                <a className="button is-info is-pulled-right is-normal" target="_blank" href={t("admin.info.enterpriseLink")}>{t("admin.info.enterpriseButton")}</a>
              </div>
            </div>
          </article>
        </div>
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

export default connect(mapStateToProps)(injectSheet(styles)(translate("admin")(AdminDetails)));
