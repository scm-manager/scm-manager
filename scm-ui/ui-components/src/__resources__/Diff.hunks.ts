///
/// MIT License
///
/// Copyright (c) 2020-present Cloudogu GmbH and Contributors
///
/// Permission is hereby granted, free of charge, to any person obtaining a copy
/// of this software and associated documentation files (the "Software"), to deal
/// in the Software without restriction, including without limitation the rights
/// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
/// copies of the Software, and to permit persons to whom the Software is
/// furnished to do so, subject to the following conditions:
///
/// The above copyright notice and this permission notice shall be included in all
/// copies or substantial portions of the Software.
///
/// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
/// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
/// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
/// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
/// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
/// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
/// SOFTWARE.
///

export default `diff --git a/src/main/java/com/cloudogu/scm/review/pullrequest/service/DefaultPullRequestService.java b/src/main/java/com/cloudogu/scm/review/pullrequest/service/DefaultPullRequestService.java
index 17c35f6..cdf70f1 100644
--- a/src/main/java/com/cloudogu/scm/review/pullrequest/service/DefaultPullRequestService.java
+++ b/src/main/java/com/cloudogu/scm/review/pullrequest/service/DefaultPullRequestService.java
@@ -25,6 +25,8 @@ import java.util.Optional;
 import java.util.Set;
 import java.util.stream.Collectors;
 
+import static com.cloudogu.scm.review.pullrequest.service.PullRequestApprovalEvent.ApprovalCause.APPROVAL_REMOVED;
+import static com.cloudogu.scm.review.pullrequest.service.PullRequestApprovalEvent.ApprovalCause.APPROVED;
 import static com.cloudogu.scm.review.pullrequest.service.PullRequestStatus.MERGED;
 import static com.cloudogu.scm.review.pullrequest.service.PullRequestStatus.OPEN;
 import static com.cloudogu.scm.review.pullrequest.service.PullRequestStatus.REJECTED;
@@ -200,6 +202,7 @@ public class DefaultPullRequestService implements PullRequestService {
     PullRequest pullRequest = getPullRequestFromStore(repository, pullRequestId);
     pullRequest.addApprover(user.getId());
     getStore(repository).update(pullRequest);
+    eventBus.post(new PullRequestApprovalEvent(repository, pullRequest, APPROVED));
   }
 
   @Override
@@ -211,8 +214,12 @@ public class DefaultPullRequestService implements PullRequestService {
     approver.stream()
       .filter(recipient -> user.getId().equals(recipient))
       .findFirst()
-      .ifPresent(pullRequest::removeApprover);
-    getStore(repository).update(pullRequest);
+      .ifPresent(
+        approval -> {
+          pullRequest.removeApprover(approval);
+          getStore(repository).update(pullRequest);
+          eventBus.post(new PullRequestApprovalEvent(repository, pullRequest, APPROVAL_REMOVED));
+        });
   }
 
   @Override`;
