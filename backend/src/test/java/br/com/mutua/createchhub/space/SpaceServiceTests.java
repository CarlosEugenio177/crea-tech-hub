package br.com.mutua.createchhub.space;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class SpaceServiceTests {

  @Mock private SpaceRepository spaces;

  @Test
  void listsOnlyActiveSpacesWithThePublicCatalogSort() {
    var space = new Space("Sala aberta", SpaceType.ROOM, 8, "Projetor");
    when(spaces.findByActive(eq(true), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(space)));

    var result = new SpaceService(spaces).listPublic(2, 10);

    assertThat(result.getContent())
        .extracting(PublicSpaceResponse::name)
        .containsExactly("Sala aberta");
    var pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(spaces).findByActive(eq(true), pageable.capture());
    assertThat(pageable.getValue().getPageNumber()).isEqualTo(2);
    assertThat(pageable.getValue().getPageSize()).isEqualTo(10);
    assertThat(pageable.getValue().getSort().getOrderFor("name")).isNotNull();
    assertThat(pageable.getValue().getSort().getOrderFor("id")).isNotNull();
  }
}
