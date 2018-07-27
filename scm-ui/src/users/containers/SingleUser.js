//@flow
import React from "react";
import { connect } from "react-redux";
import { Page } from "../../components/layout";
import { Route } from "react-router";
import { Details } from "./../components/table";
import EditUser from "./EditUser";
import type { User } from "../types/User";
import type { UserEntry } from "../types/UserEntry";
import type { History } from "history";
import { fetchUser, deleteUser } from "../modules/users";
import Loading from "../../components/Loading";

import { Navigation, Section, NavLink } from "../../components/navigation";
import { DeleteUserNavLink, EditUserNavLink } from "./../components/navLinks";
import ErrorPage from "../../components/ErrorPage";
import { translate } from "react-i18next";


type Props = {
  t: string => string,
  name: string,
  userEntry?: UserEntry,
  match: any,
  deleteUser: (user: User, callback?: () => void) => void,
  fetchUser: string => void,
  history: History
};

class SingleUser extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchUser(this.props.name);
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
    const { t, userEntry } = this.props;

    if (!userEntry || userEntry.loading) {
      return <Loading />;
    }

    if (userEntry.error) {
      return (
        <ErrorPage
          title={t("single-user.error-title")}
          subtitle={t("single-user.error-subtitle")}
          error={userEntry.error}
        />
      );
    }

    const user = userEntry.entry;
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
          </div>
          <div className="column">
            <Navigation>
              <Section label={t("single-user.navigation-label")}>
                <NavLink to={`${url}`} label={t("single-user.information-label")} />
                <EditUserNavLink user={user} editUrl={`${url}/edit`} />
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
  let userEntry;
  if (state.users && state.users.byNames) {
    userEntry = state.users.byNames[name];
  }

  return {
    name,
    userEntry
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchUser: (name: string) => {
      dispatch(fetchUser(name));
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
