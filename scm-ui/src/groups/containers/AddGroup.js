//@flow
import React from 'react';

import Page from "../../components/layout/Page"
import { translate } from "react-i18next";
import GroupForm from './GroupForm';

export interface Props {
  t: string => string
}

export interface State {
}

class AddGroup extends React.Component<Props, State> {

  render() {
    const { t } = this.props;
    return <Page title={t("add-group.title")} subtitle={t("add-group.subtitle")}><div><GroupForm /></div></Page>
  }

}

export default translate("groups")(AddGroup);
