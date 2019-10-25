import React from "react";
import { withTranslation, WithTranslation } from "react-i18next";
import { ErrorPage } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  error: Error;
};

class IndexErrorPage extends React.Component<Props> {
  render() {
    const { error, t } = this.props;
    return <ErrorPage title={t("app.error.title")} subtitle={t("app.error.subtitle")} error={error} />;
  }
}

export default withTranslation("commons")(IndexErrorPage);
