//@flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import type { History } from "history";
import queryString from "query-string";
import type { Group, PagedCollection } from "@scm-manager/ui-types";
import {
  fetchGroupsByPage,
  fetchGroupsByLink,
  getGroupsFromState,
  isFetchGroupsPending,
  getFetchGroupsFailure,
  isPermittedToCreateGroups,
  selectListAsCollection
} from "../modules/groups";
import {
  Page,
  PageActions,
  Button,
  Notification,
  LinkPaginator,
  getPageFromMatch
} from "@scm-manager/ui-components";
import { GroupTable } from "./../components/table";
import CreateGroupButton from "../components/buttons/CreateGroupButton";
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
  location: any,

  // dispatch functions
  fetchGroupsByPage: (link: string, page: number, filter?: any) => void,
  fetchGroupsByLink: (link: string) => void
};

class Groups extends React.Component<Props> {
  componentDidMount() {
    const { fetchGroupsByPage, groupLink, page } = this.props;
    fetchGroupsByPage(groupLink, page, this.getQueryString());
  }

  componentDidUpdate = (prevProps: Props) => {
    const { list, page, loading, location, fetchGroupsByPage, groupLink } = this.props;
    if (list && page && !loading) {
      const statePage: number = list.page + 1;
      if (
        page !== statePage ||
        prevProps.location.search !== location.search
      ) {
        fetchGroupsByPage(groupLink, page, this.getQueryString());
      }
    }
  };

  render() {
    const { groups, loading, error, history, t } = this.props;
    return (
      <Page
        title={t("groups.title")}
        subtitle={t("groups.subtitle")}
        loading={loading || !groups}
        error={error}
        filter={filter => {
          history.push("/groups/?q=" + filter);
        }}
      >
        {this.renderGroupTable()}
        {this.renderCreateButton()}
        {this.renderPageActionCreateButton()}
      </Page>
    );
  }

  renderGroupTable() {
    const { groups, t } = this.props;
    if (groups && groups.length > 0) {
      return (
        <>
          <GroupTable groups={groups} />
          {this.renderPaginator()}
        </>
      );
    }
    return <Notification type="info">{t("groups.noGroups")}</Notification>;
  }

  renderPaginator = () => {
    const { list, page } = this.props;
    if (list) {
      return (
        <LinkPaginator
          collection={list}
          page={page}
          filter={this.getQueryString()}
        />
      );
    }
    return null;
  };

  renderCreateButton() {
    if (this.props.canAddGroups) {
      return <CreateGroupButton />;
    }
    return null;
  }

  renderPageActionCreateButton() {
    const { canAddGroups, t } = this.props;
    if (canAddGroups) {
      return (
        <PageActions>
          <Button
            label={t("create-group-button.label")}
            link="/groups/add"
            color="primary"
          />
        </PageActions>
      );
    }
    return null;
  }

  getQueryString = () => {
    const { location } = this.props;
    return location.search ? queryString.parse(location.search).q : null;
  };
}

const mapStateToProps = (state, ownProps) => {
  const { match } = ownProps;
  const groups = getGroupsFromState(state);
  const loading = isFetchGroupsPending(state);
  const error = getFetchGroupsFailure(state);
  const page = getPageFromMatch(match);
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
    fetchGroupsByPage: (link: string, page: number, filter?: any) => {
      dispatch(fetchGroupsByPage(link, page, filter));
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
