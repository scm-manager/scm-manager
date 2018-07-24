// @flow
import React from "react";
import Page from "../../components/Page";
import { translate } from "react-i18next";

type Props = {
  t: string => string
};

class Repositories extends React.Component<Props> {
  render() {
    const { t } = this.props;
    return (
      <Page
        title={t("repositories.title")}
        subtitle={t("repositories.subtitle")}
      >
        {t("repositories.body")}
      </Page>
    );
  }
}

export default translate("repositories")(Repositories);
