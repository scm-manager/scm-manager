import React from "react";
import { translate } from "react-i18next";
import Title from "../../components/layout/Title";
import {
  fetchConfig,
  getFetchConfigFailure,
  isFetchConfigPending
} from "../modules/config";
import connect from "react-redux/es/connect/connect";
import ErrorPage from "../../components/ErrorPage";
import Loading from "../../components/Loading";

type Props = {
  loading: boolean,
  error: Error,

  // context objects
  t: string => string,
  fetchConfig: void => void
};

class GlobalConfig extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchConfig();
  }

  render() {
    const { t, error, loading } = this.props;

    if (error) {
      return (
        <ErrorPage
          title={t("global-config.error-title")}
          subtitle={t("global-config.error-subtitle")}
          error={error}
        />
      );
    }

    if (loading) {
      return <Loading />;
    }

    return <Title title={t("global-config.title")} />;
  }
}

const mapDispatchToProps = dispatch => {
  return {
    fetchConfig: () => {
      dispatch(fetchConfig());
    }
  };
};

const mapStateToProps = state => {
  const loading = isFetchConfigPending(state);
  const error = getFetchConfigFailure(state);

  return {
    loading,
    error
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("config")(GlobalConfig));
