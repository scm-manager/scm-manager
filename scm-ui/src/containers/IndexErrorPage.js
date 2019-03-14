//@flow
import React from "react";
import { translate, type TFunction } from "react-i18next";
import { ErrorPage } from "@scm-manager/ui-components";

type Props = {
  error: Error,
  t: TFunction
}

class IndexErrorPage extends React.Component<Props> {

  render() {
    const { error, t } = this.props;
    return (
      <ErrorPage
        title={t("app.error.title")}
        subtitle={t("app.error.subtitle")}
        error={error}
      />
    );
  }

}

export default translate("commons")(IndexErrorPage);
