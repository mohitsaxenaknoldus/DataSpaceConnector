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
package org.eclipse.dataspaceconnector.dataloading;

import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractDefinition;

public interface ContractDefinitionLoader extends DataSink<ContractDefinition> {
    String FEATURE = "edc:contract:contractdef:loader";
}
