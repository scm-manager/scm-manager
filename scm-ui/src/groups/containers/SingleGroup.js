//@flow
import React from "react";
import { connect } from "react-redux";
import { Page } from "../../components/layout";
import { Route } from "react-router";
import { Details } from "./../components/table";
import { DeleteGroupNavLink, EditGroupNavLink } from "./../components/navLinks";
import type { Group } from "../types/Group";
import type { History } from "history";
import {
  deleteGroup,
  fetchGroup,
  getGroupByName,
  isFetchGroupPending,
  getFetchGroupFailure,
  getDeleteGroupFailure,
  isDeleteGroupPending,
} from "../modules/groups";
import Loading from "../../components/Loading";

import { Navigation, Section, NavLink } from "../../components/navigation";
import ErrorPage from "../../components/ErrorPage";
import { translate } from "react-i18next";

type Props = {
  name: string,
  group: Group,
  loading: boolean,
  error: Error,

  // dispatcher functions
  deleteGroup: (group: Group, callback?: () => void) => void,
  fetchGroup: string => void,

  // context objects
  t: string => string,
  match: any,
  history: History
};

class SingleGroup extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchGroup(this.props.name);
  }

  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 2);
    }
    return url;
  };

  deleteGroup = (group: Group) => {
    this.props.deleteGroup(group, this.groupDeleted);
  };

  groupDeleted = () => {
    this.props.history.push("/groups");
  };

  matchedUrl = () => {
    return this.stripEndingSlash(this.props.match.url);
  };

  render() {
    const { t, loading, error, group } = this.props;

    if (error) {
      return (
        <ErrorPage
          title={t("single-group.error-title")}
          subtitle={t("single-group.error-subtitle")}
          error={error}
        />
      );
    }

    if (!group || loading) {
      return <Loading />;
    }

    const url = this.matchedUrl();

    return (
      <Page title={group.name}>
        <div className="columns">
          <div className="column is-three-quarters">
            <Route path={url} exact component={() => <Details group={group} />} />
          </div>
          <div className="column">
            <Navigation>
              <Section label={t("single-group.navigation-label")}>
                <NavLink
                  to={`${url}`}
                  label={t("single-group.information-label")}
                />
              </Section>
              <Section label={t("single-group.actions-label")}>
                <DeleteGroupNavLink group={group} deleteGroup={this.deleteGroup} />
                <EditGroupNavLink group={group} editUrl={`${url}/edit`}/>
                <NavLink to="/groups" label={t("single-group.back-label")} />
              </Section>
            </Navigation>
          </div>
        </div>
      </Page>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const name = ownProps.match.params.name;
  const group = getGroupByName(state, name);
  const loading =
    isFetchGroupPending(state, name) || isDeleteGroupPending(state, name);
  const error =
    getFetchGroupFailure(state, name) || getDeleteGroupFailure(state, name);

  return {
    name,
    group,
    loading,
    error
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchGroup: (name: string) => {
      dispatch(fetchGroup(name));
    },
    deleteGroup: (group: Group, callback?: () => void) => {
      dispatch(deleteGroup(group, callback));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("groups")(SingleGroup));
