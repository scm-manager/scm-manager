// @flow
import React from "react";
import { translate } from "react-i18next";
import { Title, Loading, ErrorNotification } from "@scm-manager/ui-components";

type Props = {
  loading: boolean,
  error: Error,

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
      <>Nothing special.</>
    );
  }
}

export default translate("admin")(AdminDetails);
