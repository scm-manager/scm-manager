import React from "react";
import styled from "styled-components";
import { devices } from "../devices";

const InfoTable = styled.table`
  @media screen and (max-width: ${devices.mobile.width}px) {
    td,
    th {
      display: block;
    }
    td {
      border-width: 0 0 1px;
    }
    th {
      border: none;
      padding-bottom: 0;
    }
    th:after {
      content: ": ";
    }
  }
`;
export default InfoTable;
