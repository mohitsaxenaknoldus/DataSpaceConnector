/*
 *  Copyright (c) 2021 Daimler TSS GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Daimler TSS GmbH - Initial Implementation
 *
 */

package org.eclipse.dataspaceconnector.ids.transform;

import de.fraunhofer.iais.eis.Artifact;
import de.fraunhofer.iais.eis.ArtifactBuilder;
import de.fraunhofer.iais.eis.CustomMediaTypeBuilder;
import de.fraunhofer.iais.eis.RepresentationBuilder;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceBuilder;
import org.eclipse.dataspaceconnector.ids.spi.transform.TransformKeys;
import org.eclipse.dataspaceconnector.ids.spi.transform.TransformerContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.mock;

public class IdsResourceToAssetTransformerTest {
    private static final String ASSET_ID = "1";
    private static final URI RESOURCE_ID = URI.create("urn:resource:1");
    private static final String ASSET_FILENAME = "test_filename";
    private static final String ASSET_FILE_EXTENSION = "txt";
    private static final BigInteger ASSET_BYTESIZE = BigInteger.valueOf(5);

    // subject
    private IdsResourceToAssetTransformer transformer;

    // mocks
    private Resource resource;
    private TransformerContext context;

    @BeforeEach
    void setUp() {
        transformer = new IdsResourceToAssetTransformer();
        Artifact artifact = new ArtifactBuilder()._byteSize_(ASSET_BYTESIZE)._fileName_(ASSET_FILENAME).build();

        var representation = new RepresentationBuilder()
                ._instance_(new ArrayList<>(Collections.singletonList(artifact)))
                ._mediaType_(new CustomMediaTypeBuilder()._filenameExtension_(ASSET_FILE_EXTENSION).build())
                .build();

        resource = new ResourceBuilder(RESOURCE_ID)._representation_(new ArrayList<>(Collections.singletonList(representation))).build();
        context = mock(TransformerContext.class);
    }

    @Test
    void testThrowsNullPointerExceptionForAll() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            transformer.transform(null, null);
        });
    }

    @Test
    void testThrowsNullPointerExceptionForContext() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            transformer.transform(resource, null);
        });
    }

    @Test
    void testReturnsNull() {
        var result = transformer.transform(null, context);

        Assertions.assertNull(result);
    }

    @Test
    void testSuccessfulMap() {
        var result = transformer.transform(resource, context);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(ASSET_ID, result.getId());
        Assertions.assertEquals(result.getProperties().get(TransformKeys.KEY_ASSET_BYTE_SIZE), ASSET_BYTESIZE);
        Assertions.assertEquals(result.getProperties().get(TransformKeys.KEY_ASSET_FILE_NAME), ASSET_FILENAME);
        Assertions.assertEquals(result.getProperties().get(TransformKeys.KEY_ASSET_FILE_EXTENSION), ASSET_FILE_EXTENSION);
    }

}
