package com.acme.jga.infra.services.impl.sectors;

import com.acme.jga.infra.converters.SectorsConverter;
import com.acme.jga.infra.dao.api.sectors.ISectorsDao;
import com.acme.jga.infra.dto.sectors.v1.SectorDb;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SectorsInfraServiceTest {
    @Mock
    SectorsConverter sectorsConverter;
    @Mock
    private ISectorsDao sectorsDao;
    @InjectMocks
    private SectorsInfraService sectorsInfraService;

    @Test
    void fetchSectorsWithHierarchy() {

        // GIVEN
        SectorDb rootSector = SectorDb.builder()
                .code("root")
                .id(1L)
                .label("root")
                .orgId(1L)
                .root(true)
                .build();

        SectorDb northSector = SectorDb.builder()
                .code("north")
                .id(2L)
                .label("north")
                .orgId(1L)
                .root(false)
                .parentId(1L)
                .build();

        SectorDb southSector = SectorDb.builder()
                .code("south")
                .id(3L)
                .label("south")
                .orgId(1L)
                .root(false)
                .parentId(1L)
                .build();

        SectorDb southEastSector = SectorDb.builder()
                .code("south-east")
                .id(4L)
                .label("south-east")
                .orgId(1L)
                .root(false)
                .parentId(3L)
                .build();

        SectorDb northWestSector = SectorDb.builder()
                .code("north-west")
                .id(5L)
                .label("north-west")
                .orgId(1L)
                .root(false)
                .parentId(2L)
                .build();

        List<SectorDb> sectors = Arrays.asList(rootSector, northSector, southSector, southEastSector,
                northWestSector);

        // WHEN

        // THEN
        sectorsInfraService.mapSectorsRecursively(rootSector, sectors);

        assertNotNull(rootSector, "Sector not null");
        assertEquals(2, rootSector.getChildren().size(), "Two children of root sector");
        assertAll(() -> {
            Optional<SectorDb> optNothSector = rootSector.getChildren().stream()
                    .filter(c -> c.getLabel().equals("north")).findFirst();
            assertTrue(optNothSector.isPresent(), "Found north sector");
            Optional<SectorDb> optSouthSector = rootSector.getChildren().stream()
                    .filter(c -> c.getLabel().equals("south")).findFirst();
            assertTrue(optSouthSector.isPresent(), "Found south sector");

            assertTrue(optNothSector.get().hasChildren(), "North sector has 1 child");
            assertTrue(optSouthSector.get().hasChildren(), "South sector has 1 child");

            Optional<SectorDb> optNorthWest = optNothSector.get().getChildren().stream()
                    .filter(child -> child.getLabel().equals("north-west")).findFirst();
            assertTrue(optNorthWest.isPresent(), "Found north west sector");

            Optional<SectorDb> optSouthEast = optSouthSector.get().getChildren().stream()
                    .filter(child -> child.getLabel().equals("south-east")).findFirst();
            assertTrue(optSouthEast.isPresent(), "Found south east sector");
        });

    }

}
