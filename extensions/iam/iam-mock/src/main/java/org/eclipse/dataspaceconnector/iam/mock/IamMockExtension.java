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

package org.eclipse.dataspaceconnector.iam.mock;

import org.eclipse.dataspaceconnector.spi.iam.IdentityService;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

import java.util.Set;

/**
 * An IAM provider mock used for testing.
 */
public class IamMockExtension implements ServiceExtension {

    @Override
    public String name() {
        return "Mock IAM";
    }

    @Override
    public Set<String> provides() {
        return Set.of(IdentityService.FEATURE);
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var region = context.getSetting("edc.mock.region", "eu");
        context.registerService(IdentityService.class, new MockIdentityService(region));
    }
}
