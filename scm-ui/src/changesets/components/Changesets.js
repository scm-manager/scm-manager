import React from "react"
import { connect } from "react-redux";
import ChangesetRow from "./ChangesetRow";
import type {Changeset} from "@scm-manager/ui-types";

import {
  fetchChangesetsByNamespaceAndName,
  getChangesets,
} from "../modules/changesets";
import { translate } from "react-i18next";
import {fetchBranchesByNamespaceAndName} from "../../repos/modules/branches";

type Props = {
  changesets: Changeset[],
  t: string => string
}

class Changesets extends React.Component<Props> {
  componentDidMount() {
    const {namespace, name} = this.props.repository;
    this.props.fetchChangesetsByNamespaceAndName(namespace, name);
    this.props.fetchBranchesByNamespaceAndName(namespace, name);
  }

  render() {
    const { t, changesets } = this.props;
    if (!changesets) {
      return null;
    }
    return <table className="table is-hoverable is-fullwidth is-striped is-bordered">
      <thead>
      <th>Changesets</th>
      </thead>
      <tbody>
      {changesets.map((changeset, index) => {
        return <ChangesetRow key={index} changeset={changeset} />;
      })}
      </tbody>
    </table>
  }
}

const mapStateToProps = (state, ownProps) => {
  const {namespace, name} = ownProps.repository;
  return {
    changesets: getChangesets(namespace, name, "", state)
  }
};

const mapDispatchToProps = dispatch => {
  return {
    fetchChangesetsByNamespaceAndName: (namespace: string, name: string) => {
      dispatch(fetchChangesetsByNamespaceAndName(namespace, name));
    },

    fetchBranchesByNamespaceAndName: (namespace: string, name: string) => {
      dispatch(fetchBranchesByNamespaceAndName(namespace, name));
    }
  }
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("changesets")(Changesets));
