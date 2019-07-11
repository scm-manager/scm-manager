// @flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import { Loading, Title, Subtitle } from "@scm-manager/ui-components";
import { getAppVersion } from "../../modules/indexResource";

type Props = {
  loading: boolean,
  error: Error,

  version: string,

  // context objects
  t: string => string
};

class AdminDetails extends React.Component<Props> {
  render() {
    const { t, loading } = this.props;

    if (loading) {
      return <Loading />;
    }

    return (
      <>
        <Title title={t("admin.information.currentAppVersion")} />
        <Subtitle subtitle={this.props.version} />
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

export default connect(mapStateToProps)(translate("admin")(AdminDetails));
