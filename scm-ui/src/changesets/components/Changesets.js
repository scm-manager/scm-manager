import React from "react"
import { connect } from "react-redux";
import ChangesetRow from "./ChangesetRow";
import type {Changeset} from "@scm-manager/ui-types";

import { fetchChangesetsByNamespaceAndName, getChangesetsForNameAndNamespaceFromState } from "../modules/changesets";
import { translate } from "react-i18next";

type Props = {
  changesets: Changeset[],
  t: string => string
}

class Changesets extends React.Component<Props> {
  componentDidMount() {
    const {namespace, name} = this.props.repository;
    this.props.fetchChangesetsByNamespaceAndName(namespace, name);
  }

  render() {
    const { t, changesets } = this.props;
    if (!changesets) {
      return null;
    }
    return <table className="table is-hoverable is-fullwidth is-striped is-bordered">
      <thead>
      <tr>Changesets</tr>
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
  return {
    changesets: getChangesetsForNameAndNamespaceFromState(ownProps.repository.namespace, ownProps.repository.name, state)
  }
};

const mapDispatchToProps = dispatch => {
  return {
    fetchChangesetsByNamespaceAndName: (namespace: string, name: string) => {
      dispatch(fetchChangesetsByNamespaceAndName(namespace, name))
    }
  }
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("changesets")(Changesets));
