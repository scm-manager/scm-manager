package sonia.scm.security;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.group.Group;
import sonia.scm.group.GroupDAO;
import sonia.scm.group.GroupNames;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupCollectorTest {

  @Mock
  private GroupDAO groupDAO;

  @InjectMocks
  private GroupCollector collector;

  @Test
  void shouldAlwaysReturnAuthenticatedGroup() {
    GroupNames groupNames = collector.collect("trillian", Collections.emptySet());
    assertThat(groupNames).containsOnly("_authenticated");
  }

  @Nested
  class WithGroupsFromDao {

    @BeforeEach
    void setUpGroupsDao() {
      List<Group> groups = Lists.newArrayList(
        new Group("xml", "heartOfGold", "trillian"),
        new Group("xml", "g42", "dent", "prefect"),
        new Group("xml", "fjordsOfAfrican", "dent", "trillian")
      );
      when(groupDAO.getAll()).thenReturn(groups);
    }

    @Test
    void shouldReturnGroupsFromDao() {
      GroupNames groupNames = collector.collect("trillian", Collections.emptySet());
      assertThat(groupNames).contains("_authenticated", "heartOfGold", "fjordsOfAfrican");
    }

    @Test
    void shouldCombineGivenWithDao() {
      GroupNames groupNames = collector.collect("trillian", ImmutableList.of("awesome", "incredible"));
      assertThat(groupNames).contains("_authenticated", "heartOfGold", "fjordsOfAfrican", "awesome", "incredible");
    }

  }

}
