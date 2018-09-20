//@flow
import React from "react";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";

type Props = {
  id: string
};

class ChangesetView extends React.Component<State, Props> {
  constructor(props) {
    super(props);
    this.state = {};
  }

  componentDidMount() {
    const id = this.props.match.params.id;
  }

  render() {
    const id = this.props.match.params.id;

    return <div>Hallo! Changesets here! {id}</div>;
  }
}

const mapStateToProps = (state, ownProps: Props) => {
  return null;
};

const mapDispatchToProps = dispatch => {
  return null;
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(ChangesetView)
);
