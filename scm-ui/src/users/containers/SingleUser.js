//@flow
import React from "react";
import { connect } from "react-redux";
import {
  Page,
  Loading,
  Navigation,
  Section,
  NavLink,
  ErrorPage
} from "@scm-manager/ui-components";
import { Route } from "react-router";
import { Details } from "./../components/table";
import EditUser from "./EditUser";
import type { User } from "@scm-manager/ui-types";
import type { History } from "history";
import {
  fetchUserByName,
  deleteUser,
  getUserByName,
  isFetchUserPending,
  getFetchUserFailure,
  isDeleteUserPending,
  getDeleteUserFailure
} from "../modules/users";

import {
  DeleteUserNavLink,
  EditUserNavLink,
  SetPasswordNavLink
} from "./../components/navLinks";
import { translate } from "react-i18next";
import { getUsersLink } from "../../modules/indexResource";
import SetUserPassword from "../components/SetUserPassword";

type Props = {
  name: string,
  user: User,
  loading: boolean,
  error: Error,
  usersLink: string,

  // dispatcher functions
  deleteUser: (user: User, callback?: () => void) => void,
  fetchUserByName: (string, string) => void,

  // context objects
  t: string => string,
  match: any,
  history: History
};

class SingleUser extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchUserByName(this.props.usersLink, this.props.name);
  }

  userDeleted = () => {
    this.props.history.push("/users");
  };

  deleteUser = (user: User) => {
    this.props.deleteUser(user, this.userDeleted);
  };

  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 2);
    }
    return url;
  };

  matchedUrl = () => {
    return this.stripEndingSlash(this.props.match.url);
  };

  render() {
    const { t, loading, error, user } = this.props;

    if (error) {
      return (
        <ErrorPage
          title={t("single-user.error-title")}
          subtitle={t("single-user.error-subtitle")}
          error={error}
        />
      );
    }

    if (!user || loading) {
      return <Loading />;
    }

    const url = this.matchedUrl();

    return (
      <Page title={user.displayName}>
        <div className="columns">
          <div className="column is-three-quarters">
            <Route path={url} exact component={() => <Details user={user} />} />
            <Route
              path={`${url}/edit`}
              component={() => <EditUser user={user} />}
            />
            <Route
              path={`${url}/password`}
              component={() => <SetUserPassword user={user} />}
            />
          </div>
          <div className="column">
            <Navigation>
              <Section label={t("single-user.navigation-label")}>
                <NavLink
                  to={`${url}`}
                  label={t("single-user.information-label")}
                />
                <EditUserNavLink user={user} editUrl={`${url}/edit`} />
                <SetPasswordNavLink
                  user={user}
                  passwordUrl={`${url}/password`}
                />
              </Section>
              <Section label={t("single-user.actions-label")}>
                <DeleteUserNavLink user={user} deleteUser={this.deleteUser} />
                <NavLink to="/users" label={t("single-user.back-label")} />
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
  const user = getUserByName(state, name);
  const loading =
    isFetchUserPending(state, name) || isDeleteUserPending(state, name);
  const error =
    getFetchUserFailure(state, name) || getDeleteUserFailure(state, name);
  const usersLink = getUsersLink(state);
  return {
    usersLink,
    name,
    user,
    loading,
    error
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchUserByName: (link: string, name: string) => {
      dispatch(fetchUserByName(link, name));
    },
    deleteUser: (user: User, callback?: () => void) => {
      dispatch(deleteUser(user, callback));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("users")(SingleUser));
