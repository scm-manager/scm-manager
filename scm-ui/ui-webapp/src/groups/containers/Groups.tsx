import React from 'react';
import { connect } from 'react-redux';
import { translate } from 'react-i18next';
import { History } from 'history';
import { Group, PagedCollection } from '@scm-manager/ui-types';
import {
  fetchGroupsByPage,
  getGroupsFromState,
  isFetchGroupsPending,
  getFetchGroupsFailure,
  isPermittedToCreateGroups,
  selectListAsCollection,
} from '../modules/groups';
import {
  Page,
  PageActions,
  OverviewPageActions,
  Notification,
  LinkPaginator,
  urls,
  CreateButton,
} from '@scm-manager/ui-components';
import { GroupTable } from './../components/table';
import { getGroupsLink } from '../../modules/indexResource';

type Props = {
  groups: Group[];
  loading: boolean;
  error: Error;
  canAddGroups: boolean;
  list: PagedCollection;
  page: number;
  groupLink: string;

  // context objects
  t: (p: string) => string;
  history: History;
  location: any;

  // dispatch functions
  fetchGroupsByPage: (link: string, page: number, filter?: string) => void;
};

class Groups extends React.Component<Props> {
  componentDidMount() {
    const { fetchGroupsByPage, groupLink, page, location } = this.props;
    fetchGroupsByPage(
      groupLink,
      page,
      urls.getQueryStringFromLocation(location),
    );
  }

  componentDidUpdate = (prevProps: Props) => {
    const {
      loading,
      list,
      page,
      groupLink,
      location,
      fetchGroupsByPage,
    } = this.props;
    if (list && page && !loading) {
      const statePage: number = list.page + 1;
      if (page !== statePage || prevProps.location.search !== location.search) {
        fetchGroupsByPage(
          groupLink,
          page,
          urls.getQueryStringFromLocation(location),
        );
      }
    }
  };

  render() {
    const { groups, loading, error, canAddGroups, t } = this.props;
    return (
      <Page
        title={t('groups.title')}
        subtitle={t('groups.subtitle')}
        loading={loading || !groups}
        error={error}
      >
        {this.renderGroupTable()}
        {this.renderCreateButton()}
        <PageActions>
          <OverviewPageActions
            showCreateButton={canAddGroups}
            link="groups"
            label={t('create-group-button.label')}
          />
        </PageActions>
      </Page>
    );
  }

  renderGroupTable() {
    const { groups, list, page, location, t } = this.props;
    if (groups && groups.length > 0) {
      return (
        <>
          <GroupTable groups={groups} />
          <LinkPaginator
            collection={list}
            page={page}
            filter={urls.getQueryStringFromLocation(location)}
          />
        </>
      );
    }
    return <Notification type="info">{t('groups.noGroups')}</Notification>;
  }

  renderCreateButton() {
    const { canAddGroups, t } = this.props;
    if (canAddGroups) {
      return (
        <CreateButton
          label={t('create-group-button.label')}
          link="/groups/create"
        />
      );
    }
    return null;
  }
}

const mapStateToProps = (state, ownProps) => {
  const { match } = ownProps;
  const groups = getGroupsFromState(state);
  const loading = isFetchGroupsPending(state);
  const error = getFetchGroupsFailure(state);
  const page = urls.getPageFromMatch(match);
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
    groupLink,
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchGroupsByPage: (link: string, page: number, filter?: string) => {
      dispatch(fetchGroupsByPage(link, page, filter));
    },
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps,
)(translate('groups')(Groups));
