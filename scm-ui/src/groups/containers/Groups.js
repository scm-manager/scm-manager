//@flow
import React from "react";
import { connect } from "react-redux";

type Props = {};

class Groups extends React.Component<Props> {
  render() {
    return "groups will be displayed here";
  }
}

const mapStateToProps = state => {
  return {};
};

const mapDispatchToProps = (dispatch) => {
  return {};
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Groups);
