#
# MIT License
#
# Copyright (c) 2020-present Cloudogu GmbH and Contributors
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#

Feature: Anonymous Mode Full

  Background:
    Given Full Anonymous Mode is enabled
    And User is not authenticated

  Scenario: Show login button on anonymous route
    Given Anonymous user has permission to read all repositories
    When User visits the repository overview page
    Then The repository overview page is shown
    And There is a login button

  Scenario: Show login page
    When User visits login page
    Then The login page is shown

  Scenario: Navigate to login page
    When Users clicks login button
    Then The login page is shown

  Scenario: Redirect to repositories overview after login
    When User logs in
    Then User should be authenticated

  Scenario: Anonymous user cannot change password
    When User visits their user settings
    Then There is no option to change the password
