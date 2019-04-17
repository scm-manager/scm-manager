//@flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import { compose } from "redux";
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

type State = {
  page: number
};

class Groups extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      page: -1
    };
  }

  componentDidMount() {
    const { fetchGroupsByPage, groupLink, page } = this.props;
    fetchGroupsByPage(groupLink, page, this.getQueryString());
    this.setState({ page: page });
  }

  componentDidUpdate = (prevProps: Props) => {
    const { list, page, location, fetchGroupsByPage, groupLink } = this.props;
    if (list && page) {
      if (
        page !== this.state.page ||
        prevProps.location.search !== location.search
      ) {
        fetchGroupsByPage(groupLink, page, this.getQueryString());
        this.setState({ page: page });
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
        <GroupTable groups={groups} />
        {this.renderPaginator()}
        {this.renderCreateButton()}
        {this.renderPageActionCreateButton()}
      </Page>
    );
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

export default compose(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )
)(translate("groups")(Groups));
