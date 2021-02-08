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
import React, {FC, useEffect} from "react";
import { connect } from "react-redux";
import {Redirect, useLocation, withRouter} from "react-router-dom";
import {useTranslation, WithTranslation, withTranslation} from "react-i18next";
import queryString from "query-string";
import { History } from "history";
import { Branch, BranchRequest, Repository } from "@scm-manager/ui-types";
import { ErrorNotification, Loading, Subtitle } from "@scm-manager/ui-components";
import BranchForm from "../components/BranchForm";
import {
  createBranch,
  createBranchReset,
  fetchBranches,
  getBranchCreateLink,
  getBranches,
  getCreateBranchFailure,
  getFetchBranchesFailure,
  isCreateBranchPending,
  isFetchBranchesPending
} from "../modules/branches";
import { compose } from "redux";
import {useBranches, useCreateBranch} from "@scm-manager/ui-api";

type PropsClass = WithTranslation & {
  loading?: boolean;
  error?: Error;
  repository: Repository;
  branches: Branch[];
  createBranchesLink: string;
  isPermittedToCreateBranches: boolean;

  // dispatcher functions
  fetchBranches: (p: Repository) => void;
  createBranch: (
    createLink: string,
    repository: Repository,
    branch: BranchRequest,
    callback?: (p: Branch) => void
  ) => void;
  resetForm: (p: Repository) => void;

  // context objects
  history: History;
  location: any;
};

type Props = {
  repository: Repository;
};

const CreateBranch: FC<Props> = ({repository}) => {
  const {isLoading: isLoadingCreate, error: errorCreate, create, branch: createdBranch} = useCreateBranch(repository);
  const {isLoading: isLoadingList, error: errorList, data: branches} = useBranches(repository);
  const location = useLocation();
  const [t] = useTranslation("repos");

  const transmittedName = (url: string) => {
    const params = queryString.parse(url);
    return params.name;
  };

  if (createdBranch) {
    return <Redirect to={`/repo/${repository.namespace}/${repository.name}/branch/${encodeURIComponent(createdBranch.name)}/info`}/>;
  }

  if (errorList) {
    return <ErrorNotification error={errorList} />;
  }

  if (errorCreate) {
    return <ErrorNotification error={errorCreate} />;
  }

  if (isLoadingList || !branches) {
    return <Loading />;
  }

  return (
    <>
      <Subtitle subtitle={t("branches.create.title")} />
      <BranchForm
        submitForm={create}
        loading={isLoadingCreate}
        repository={repository}
        branches={branches._embedded.branches}
        transmittedName={transmittedName(location.search)}
      />
    </>
  );
};

class CreateBranchClass extends React.Component<PropsClass> {
  componentDidMount() {
    const { fetchBranches, repository } = this.props;
    fetchBranches(repository);
    this.props.resetForm(repository);
  }

  branchCreated = (branch: Branch) => {
    const { history, repository } = this.props;
    history.push(`/repo/${repository.namespace}/${repository.name}/branch/${encodeURIComponent(branch.name)}/info`);
  };

  createBranch = (branch: BranchRequest) => {
    this.props.createBranch(this.props.createBranchesLink, this.props.repository, branch, newBranch =>
      this.branchCreated(newBranch)
    );
  };

  transmittedName = (url: string) => {
    const params = queryString.parse(url);
    return params.name;
  };

  render() {
    const { t, loading, error, repository, branches, createBranchesLink, location } = this.props;

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (loading || !branches) {
      return <Loading />;
    }

    return (
      <>
        <Subtitle subtitle={t("branches.create.title")} />
        <BranchForm
          submitForm={branchRequest => this.createBranch(branchRequest)}
          loading={loading}
          repository={repository}
          branches={branches}
          transmittedName={this.transmittedName(location.search)}
          disabled={!createBranchesLink}
        />
      </>
    );
  }
}

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchBranches: (repository: Repository) => {
      dispatch(fetchBranches(repository));
    },
    createBranch: (
      createLink: string,
      repository: Repository,
      branchRequest: BranchRequest,
      callback?: (newBranch: Branch) => void
    ) => {
      dispatch(createBranch(createLink, repository, branchRequest, callback));
    },
    resetForm: (repository: Repository) => {
      dispatch(createBranchReset(repository));
    }
  };
};

const mapStateToProps = (state: any, ownProps: PropsClass) => {
  const { repository } = ownProps;
  const loading = isFetchBranchesPending(state, repository) || isCreateBranchPending(state, repository);
  const error = getFetchBranchesFailure(state, repository) || getCreateBranchFailure(state, repository);
  const branches = getBranches(state, repository);
  const createBranchesLink = getBranchCreateLink(state, repository);
  return {
    repository,
    loading,
    error,
    branches,
    createBranchesLink
  };
};

export default compose(
  withTranslation("repos"),
  connect(mapStateToProps, mapDispatchToProps),
  withRouter
)(CreateBranchClass);
