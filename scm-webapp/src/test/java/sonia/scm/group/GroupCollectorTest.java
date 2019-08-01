package sonia.scm.group;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.GroupCollector;

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
    Iterable<String> groupNames = collector.collect("trillian");
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
      Iterable<String> groupNames = collector.collect("trillian");
      assertThat(groupNames).contains("_authenticated", "heartOfGold", "fjordsOfAfrican");
    }

    @Test
    void shouldCombineGivenWithDao() {
      Iterable<String> groupNames = collector.collect("trillian");
      assertThat(groupNames).contains("_authenticated", "heartOfGold", "fjordsOfAfrican", "awesome", "incredible");
    }

  }

}
