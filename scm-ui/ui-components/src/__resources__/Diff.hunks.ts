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
