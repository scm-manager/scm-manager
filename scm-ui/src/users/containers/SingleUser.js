//@flow
import React from "react";
import { connect } from "react-redux";
import { Page } from "../../components/layout";
import { Route } from "react-router";
import { Details } from "./../components/table";
import EditUser from "./EditUser";
import type { User } from "../types/User";
import type { UserEntry } from "../types/UserEntry";
import { fetchUser, deleteUser } from "../modules/users";
import Loading from "../../components/Loading";

import { Navigation, Section, NavLink } from "../../components/navigation";
import { DeleteUserButton } from "./../components/buttons";
import ErrorPage from "../../components/ErrorPage";

type Props = {
  name: string,
  userEntry?: UserEntry,
  match: any,
  deleteUser: (user: User) => void,
  fetchUser: string => void
};

class SingleUser extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchUser(this.props.name);
  }

  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 2);
    }
    return url;
  };

  render() {
    const { userEntry, match, deleteUser } = this.props;

    if (!userEntry || userEntry.loading) {
      return <Loading />;
    }

    if (userEntry.error) {
      return (
        <ErrorPage
          title="Error"
          subtitle="Unknown user error"
          error={userEntry.error}
        />
      );
    }

    const user = userEntry.entry;
    const url = this.stripEndingSlash(match.url);

    // TODO i18n

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
              <Section label="Navigation">
                <NavLink to={`${url}`} label="Information" />
                <NavLink to={`${url}/edit`} label="Edit" />
              </Section>
              <Section label="Actions">
                <DeleteUserButton user={user} deleteUser={deleteUser} />
                <NavLink to="/users" label="Back" />
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
    deleteUser: (user: User) => {
      dispatch(deleteUser(user));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(SingleUser);
