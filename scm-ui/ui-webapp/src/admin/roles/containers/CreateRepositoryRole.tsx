import React from "react";
import { connect } from "react-redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { History } from "history";
import { RepositoryRole } from "@scm-manager/ui-types";
import { ErrorNotification, Subtitle, Title } from "@scm-manager/ui-components";
import { createRole, getCreateRoleFailure, getFetchVerbsFailure, isFetchVerbsPending } from "../modules/roles";
import { getRepositoryRolesLink, getRepositoryVerbsLink } from "../../../modules/indexResource";
import RepositoryRoleForm from "./RepositoryRoleForm";

type Props = WithTranslation & {
  repositoryRolesLink: string;
  error?: Error;
  history: History;

  // dispatch function
  addRole: (link: string, role: RepositoryRole, callback?: () => void) => void;
};

class CreateRepositoryRole extends React.Component<Props> {
  repositoryRoleCreated = (role: RepositoryRole) => {
    const { history } = this.props;
    history.push("/admin/role/" + role.name + "/info");
  };

  createRepositoryRole = (role: RepositoryRole) => {
    this.props.addRole(this.props.repositoryRolesLink, role, () => this.repositoryRoleCreated(role));
  };

  render() {
    const { t, error } = this.props;

    if (error) {
      return <ErrorNotification error={error} />;
    }

    return (
      <>
        <Title title={t("repositoryRole.title")} />
        <Subtitle subtitle={t("repositoryRole.createSubtitle")} />
        <RepositoryRoleForm submitForm={role => this.createRepositoryRole(role)} />
      </>
    );
  }
}

const mapStateToProps = (state) => {
  const loading = isFetchVerbsPending(state);
  const error = getFetchVerbsFailure(state) || getCreateRoleFailure(state);
  const verbsLink = getRepositoryVerbsLink(state);
  const repositoryRolesLink = getRepositoryRolesLink(state);

  return {
    loading,
    error,
    verbsLink,
    repositoryRolesLink
  };
};

const mapDispatchToProps = dispatch => {
  return {
    addRole: (link: string, role: RepositoryRole, callback?: () => void) => {
      dispatch(createRole(link, role, callback));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withTranslation("admin")(CreateRepositoryRole));
