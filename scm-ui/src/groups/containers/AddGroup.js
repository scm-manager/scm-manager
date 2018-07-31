//@flow
import React from "react";

import Page from "../../components/layout/Page";
import { translate } from "react-i18next";
import GroupForm from "./GroupForm";
import { connect } from "react-redux";
import { createGroup } from "../modules/groups";

export interface Props {
  t: string => string;
}

export interface State {}

class AddGroup extends React.Component<Props, State> {
  render() {
    const { t } = this.props;
    return (
      <Page title={t("add-group.title")} subtitle={t("add-group.subtitle")}>
        <div>
          <GroupForm submitForm={group => this.createGroup(group)} />
        </div>
      </Page>
    );
  }

  createGroup = (group: Group) => {
    this.props.createGroup(group);
  };
}

const mapDispatchToProps = dispatch => {
  return {
    createGroup: (group: Group) => dispatch(createGroup(group))
  };
};

const mapStateToProps = state => {};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("groups")(AddGroup));
