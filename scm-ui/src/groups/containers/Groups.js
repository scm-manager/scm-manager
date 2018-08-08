//@flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import type { Group } from "../types/Group.js";
import type { PagedCollection } from "../../types/Collection";
import type { History } from "history";
import { Page } from "../../components/layout";
import { GroupTable } from "./../components/table";
import Paginator from "../../components/Paginator";
import CreateGroupButton from "../components/buttons/CreateGroupButton";

import {
  fetchGroupsByPage,
  fetchGroupsByLink,
  getGroupsFromState,
  isFetchGroupsPending,
  getFetchGroupsFailure,
  isPermittedToCreateGroups,
  selectListAsCollection
} from "../modules/groups";

type Props = {
  groups: Group[],
  loading: boolean,
  error: Error,
  canAddGroups: boolean,
  list: PagedCollection,
  page: number,

  // context objects
  t: string => string,
  history: History,

  // dispatch functions
  fetchGroupsByPage: (page: number) => void,
  fetchGroupsByLink: (link: string) => void
};

class Groups extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchGroupsByPage(this.props.page);
  }

  onPageChange = (link: string) => {
    this.props.fetchGroupsByLink(link);
  };

  /**
   * reflect page transitions in the uri
   */
  componentDidUpdate = (prevProps: Props) => {
    const { page, list } = this.props;
    if (list.page >= 0) {
      // backend starts paging by 0
      const statePage: number = list.page + 1;
      if (page !== statePage) {
        this.props.history.push(`/groups/${statePage}`);
      }
    }
  };

  render() {
    const { groups, loading, error, t } = this.props;
    return (
      <Page
        title={t("groups.title")}
        subtitle={t("groups.subtitle")}
        loading={loading || !groups}
        error={error}
      >
        <GroupTable groups={groups} />
        {this.renderPaginator()}
        {this.renderCreateButton()}
      </Page>
    );
  }

  renderPaginator() {
    const { list } = this.props;
    if (list) {
      return <Paginator collection={list} onPageChange={this.onPageChange} />;
    }
    return null;
  }

  renderCreateButton() {
    if (this.props.canAddGroups) {
      return <CreateGroupButton />;
    } else {
      return;
    }
  }
}

const getPageFromProps = props => {
  let page = props.match.params.page;
  if (page) {
    page = parseInt(page, 10);
  } else {
    page = 1;
  }
  return page;
};

const mapStateToProps = (state, ownProps) => {
  const groups = getGroupsFromState(state);
  const loading = isFetchGroupsPending(state);
  const error = getFetchGroupsFailure(state);

  const page = getPageFromProps(ownProps);
  const canAddGroups = isPermittedToCreateGroups(state);
  const list = selectListAsCollection(state);

  return {
    groups,
    loading,
    error,
    canAddGroups,
    list,
    page
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchGroupsByPage: (page: number) => {
      dispatch(fetchGroupsByPage(page));
    },
    fetchGroupsByLink: (link: string) => {
      dispatch(fetchGroupsByLink(link));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("groups")(Groups));
