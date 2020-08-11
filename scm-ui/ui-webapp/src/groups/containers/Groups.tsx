/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import { connect } from "react-redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { RouteComponentProps } from "react-router-dom";
import { Group, PagedCollection } from "@scm-manager/ui-types";
import {
  CreateButton,
  LinkPaginator,
  Notification,
  OverviewPageActions,
  Page,
  PageActions,
  urls
} from "@scm-manager/ui-components";
import { getGroupsLink } from "../../modules/indexResource";
import {
  fetchGroupsByPage,
  getFetchGroupsFailure,
  getGroupsFromState,
  isFetchGroupsPending,
  isPermittedToCreateGroups,
  selectListAsCollection
} from "../modules/groups";
import { GroupTable } from "./../components/table";

type Props = RouteComponentProps &
  WithTranslation & {
    groups: Group[];
    loading: boolean;
    error: Error;
    canAddGroups: boolean;
    list: PagedCollection;
    page: number;
    groupLink: string;

    // dispatch functions
    fetchGroupsByPage: (link: string, page: number, filter?: string) => void;
  };

class Groups extends React.Component<Props> {
  componentDidMount() {
    const { fetchGroupsByPage, groupLink, page, location } = this.props;
    fetchGroupsByPage(groupLink, page, urls.getQueryStringFromLocation(location));
  }

  componentDidUpdate = (prevProps: Props) => {
    const { loading, list, page, groupLink, location, fetchGroupsByPage } = this.props;
    if (list && page && !loading) {
      const statePage: number = this.resolveStatePage();
      if (page !== statePage || prevProps.location.search !== location.search) {
        fetchGroupsByPage(groupLink, page, urls.getQueryStringFromLocation(location));
      }
    }
  };

  resolveStatePage = () => {
    const { list } = this.props;
    if (list.page) {
      return list.page + 1;
    }
    // set page to 1 if undefined, because if groups couldn't be fetched it would lead to an fetch-loop otherwise
    return 1;
  };

  render() {
    const { groups, loading, error, canAddGroups, t } = this.props;
    return (
      <Page title={t("groups.title")} subtitle={t("groups.subtitle")} loading={loading || !groups} error={error}>
        {this.renderGroupTable()}
        {this.renderCreateButton()}
        <PageActions>
          <OverviewPageActions showCreateButton={canAddGroups} link="groups" label={t("create-group-button.label")} />
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
          <LinkPaginator collection={list} page={page} filter={urls.getQueryStringFromLocation(location)} />
        </>
      );
    }
    return <Notification type="info">{t("groups.noGroups")}</Notification>;
  }

  renderCreateButton() {
    const { canAddGroups, t } = this.props;
    if (canAddGroups) {
      return <CreateButton label={t("create-group-button.label")} link="/groups/create" />;
    }
    return null;
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
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
    groupLink
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchGroupsByPage: (link: string, page: number, filter?: string) => {
      dispatch(fetchGroupsByPage(link, page, filter));
    }
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(withTranslation("groups")(Groups));
