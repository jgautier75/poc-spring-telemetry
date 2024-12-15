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

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertAll;

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

        assertNotNull("Sector not null", rootSector);
        assertEquals("Two children of root sector", 2, rootSector.getChildren().size());
        assertAll(() -> {
            Optional<SectorDb> optNothSector = rootSector.getChildren().stream()
                    .filter(c -> c.getLabel().equals("north")).findFirst();
            assertTrue("Found north sector", optNothSector.isPresent());
            Optional<SectorDb> optSouthSector = rootSector.getChildren().stream()
                    .filter(c -> c.getLabel().equals("south")).findFirst();
            assertTrue("Found south sector", optSouthSector.isPresent());

            assertTrue("North sector has 1 child", optNothSector.get().hasChildren());
            assertTrue("South sector has 1 child", optSouthSector.get().hasChildren());

            Optional<SectorDb> optNorthWest = optNothSector.get().getChildren().stream()
                    .filter(child -> child.getLabel().equals("north-west")).findFirst();
            assertTrue("Found north west sector", optNorthWest.isPresent());

            Optional<SectorDb> optSouthEast = optSouthSector.get().getChildren().stream()
                    .filter(child -> child.getLabel().equals("south-east")).findFirst();
            assertTrue("Found south east sector", optSouthEast.isPresent());
        });

    }

}
