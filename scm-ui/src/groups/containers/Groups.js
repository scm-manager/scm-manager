//@flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import type { Group } from "@scm-manager/ui-types";
import type { PagedCollection } from "@scm-manager/ui-types";
import type { History } from "history";
import {
  Page,
  PageActions,
  Button,
  Paginator
} from "@scm-manager/ui-components";
import { GroupTable } from "./../components/table";
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
import { getGroupsLink } from "../../modules/indexResource";

type Props = {
  groups: Group[],
  loading: boolean,
  error: Error,
  canAddGroups: boolean,
  list: PagedCollection,
  page: number,
  groupLink: string,

  // context objects
  t: string => string,
  history: History,

  // dispatch functions
  fetchGroupsByPage: (link: string, page: number) => void,
  fetchGroupsByLink: (link: string) => void
};

class Groups extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchGroupsByPage(this.props.groupLink, this.props.page);
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
        {this.renderPageActionCreateButton()}
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
      return (
        <CreateGroupButton />
      );
    } else {
      return;
    }
  }

  renderPageActionCreateButton() {
    if (this.props.canAddGroups) {
      return (
        <PageActions>
          <Button
            label={this.props.t("create-group-button.label")}
            link="/groups/add"
            color="primary"
          />
        </PageActions>
      );
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

  const groupLink = getGroupsLink(state);

  return {
    groups,
    loading,
    error,
    canAddGroups,
    list,
    page,
    groupLink
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchGroupsByPage: (link: string, page: number) => {
      dispatch(fetchGroupsByPage(link, page));
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
