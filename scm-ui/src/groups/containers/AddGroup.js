//@flow
import React from "react";

import Page from "../../components/layout/Page";
import { translate } from "react-i18next";
import GroupForm from "../components/GroupForm";
import { connect } from "react-redux";
import { createGroup } from "../modules/groups";
import type { Group } from "../types/Group";
import type { History } from "history";

type Props = {
  t: string => string,
  createGroup: (group: Group, callback?: () => void) => void,
  history: History
};

type State = {};

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

  groupCreated = () => {
    this.props.history.push("/groups");
  };
  createGroup = (group: Group) => {
    this.props.createGroup(group, this.groupCreated);
  };
}

const mapDispatchToProps = dispatch => {
  return {
    createGroup: (group: Group, callback?: () => void) =>
      dispatch(createGroup(group, callback))
  };
};

const mapStateToProps = state => {
  return {}
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("groups")(AddGroup));
