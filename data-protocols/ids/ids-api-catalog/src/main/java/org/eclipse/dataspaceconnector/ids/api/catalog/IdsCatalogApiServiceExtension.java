/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.dataspaceconnector.ids.api.catalog;

import de.fraunhofer.iais.eis.DescriptionRequestMessageImpl;
import de.fraunhofer.iais.eis.DescriptionResponseMessageImpl;
import org.eclipse.dataspaceconnector.ids.spi.daps.DapsService;
import org.eclipse.dataspaceconnector.ids.spi.descriptor.IdsDescriptorService;
import org.eclipse.dataspaceconnector.ids.spi.policy.IdsPolicyService;
import org.eclipse.dataspaceconnector.spi.asset.AssetIndex;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.policy.PolicyRegistry;
import org.eclipse.dataspaceconnector.spi.protocol.web.WebService;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

import java.util.Set;

/**
 * Implements the IDS Controller REST API for catalog services.
 */
public class IdsCatalogApiServiceExtension implements ServiceExtension {
    private Monitor monitor;

    @Override
    public String name() {
        return "IDS Catalog API";
    }

    @Override
    public Set<String> requires() {
        return Set.of("edc:ids:core", PolicyRegistry.FEATURE);
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor = context.getMonitor();

        registerTypes(context);

        registerControllers(context);
    }

    private void registerTypes(ServiceExtensionContext context) {
        context.getTypeManager().registerTypes(DescriptionRequestMessageImpl.class);
        context.getTypeManager().registerTypes(DescriptionResponseMessageImpl.class);
    }

    private void registerControllers(ServiceExtensionContext context) {
        var webService = context.getService(WebService.class);
        var descriptorService = context.getService(IdsDescriptorService.class);
        var assetIndex = context.getService(AssetIndex.class);

        var dapsService = context.getService(DapsService.class);
        var policyRegistry = context.getService(PolicyRegistry.class);
        var policyService = context.getService(IdsPolicyService.class);
        var queryEngine = new QueryEngineImpl(policyRegistry, policyService, assetIndex, monitor);


        webService.registerController(new DescriptionRequestController(descriptorService));
        webService.registerController(new CatalogQueryController(queryEngine, dapsService));
    }


}
