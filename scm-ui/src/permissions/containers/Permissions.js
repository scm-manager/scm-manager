//@flow
import React from "react";
import connect from "react-redux/es/connect/connect";
import { translate } from "react-i18next";
import { fetchPermissions } from "../modules/permissions";

type Props = {
  namespace: string,
  name: string,

  //dispatch functions
  fetchPermissions: (namespace: string, name: string) => void
};

class Permissions extends React.Component<Props> {
  componentDidMount() {
    const { fetchPermissions, namespace, name } = this.props;

    fetchPermissions(namespace, name);
  }

  render() {
    return <div>Permissions will be shown here!</div>;
  }
}

const mapStateToProps = (state, ownProps) => {
  // const { namespace, name } = ownProps.match.params;
  return {
    //namespace,
    //name
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchPermissions: (namespace: string, name: string) => {
      dispatch(fetchPermissions(namespace, name));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("repos")(Permissions));
