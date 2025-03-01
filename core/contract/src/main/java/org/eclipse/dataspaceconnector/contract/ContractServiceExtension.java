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
 *       Daimler TSS GmbH - Initial API and Implementation
 *       Fraunhofer Institute for Software and Systems Engineering - add contract negotiation functionality
 *
 */

package org.eclipse.dataspaceconnector.contract;

import org.eclipse.dataspaceconnector.contract.agent.ParticipantAgentServiceImpl;
import org.eclipse.dataspaceconnector.contract.negotiation.ConsumerContractNegotiationManagerImpl;
import org.eclipse.dataspaceconnector.contract.negotiation.ProviderContractNegotiationManagerImpl;
import org.eclipse.dataspaceconnector.contract.offer.ContractDefinitionServiceImpl;
import org.eclipse.dataspaceconnector.contract.offer.ContractOfferServiceImpl;
import org.eclipse.dataspaceconnector.contract.policy.PolicyEngineImpl;
import org.eclipse.dataspaceconnector.contract.validation.ContractValidationServiceImpl;
import org.eclipse.dataspaceconnector.core.base.ExponentialWaitStrategy;
import org.eclipse.dataspaceconnector.core.base.RemoteMessageDispatcherRegistryImpl;
import org.eclipse.dataspaceconnector.spi.asset.AssetIndex;
import org.eclipse.dataspaceconnector.spi.contract.agent.ParticipantAgentService;
import org.eclipse.dataspaceconnector.spi.contract.negotiation.ConsumerContractNegotiationManager;
import org.eclipse.dataspaceconnector.spi.contract.negotiation.ContractNegotiationManager;
import org.eclipse.dataspaceconnector.spi.contract.negotiation.NegotiationWaitStrategy;
import org.eclipse.dataspaceconnector.spi.contract.negotiation.ProviderContractNegotiationManager;
import org.eclipse.dataspaceconnector.spi.contract.negotiation.store.ContractNegotiationStore;
import org.eclipse.dataspaceconnector.spi.contract.offer.ContractDefinitionService;
import org.eclipse.dataspaceconnector.spi.contract.offer.ContractOfferService;
import org.eclipse.dataspaceconnector.spi.contract.offer.store.ContractDefinitionStore;
import org.eclipse.dataspaceconnector.spi.contract.policy.PolicyEngine;
import org.eclipse.dataspaceconnector.spi.contract.validation.ContractValidationService;
import org.eclipse.dataspaceconnector.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiation;

import java.util.Set;

public class ContractServiceExtension implements ServiceExtension {

    private Monitor monitor;
    private ServiceExtensionContext context;
    private ContractDefinitionServiceImpl definitionService;

    private static final long DEFAULT_ITERATION_WAIT = 5000; // millis
    private ConsumerContractNegotiationManagerImpl consumerNegotiationManager;
    private ProviderContractNegotiationManagerImpl providerNegotiationManager;

    @Override
    public String name() {
        return "Core Contract Service";
    }

    @Override
    public final Set<String> provides() {
        return Set.of(ContractNegotiationManager.FEATURE);
    }

    @Override
    public Set<String> requires() {
        return Set.of(AssetIndex.FEATURE, ContractDefinitionStore.FEATURE);
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor = context.getMonitor();
        this.context = context;

        registerTypes(context);
        registerServices(context);
    }

    @Override
    public void start() {
        // Start negotiation managers.
        var negotiationStore = context.getService(ContractNegotiationStore.class);
        consumerNegotiationManager.start(negotiationStore);
        providerNegotiationManager.start(negotiationStore);
    }

    @Override
    public void shutdown() {
        if (consumerNegotiationManager != null) {
            consumerNegotiationManager.stop();
        }

        if (providerNegotiationManager != null) {
            providerNegotiationManager.stop();
        }
    }

    private void registerServices(ServiceExtensionContext context) {
        var assetIndex = context.getService(AssetIndex.class, true);
        if (assetIndex == null) {
            monitor.warning("No AssetIndex registered. Register one to create Contract Offers.");
            assetIndex = new NullAssetIndex();
        }

        var store = context.getService(ContractDefinitionStore.class);

        var agentService = new ParticipantAgentServiceImpl();
        context.registerService(ParticipantAgentService.class, agentService);

        var policyEngine = new PolicyEngineImpl();
        context.registerService(PolicyEngine.class, policyEngine);

        definitionService = new ContractDefinitionServiceImpl(monitor, store, policyEngine);
        var contractOfferService = new ContractOfferServiceImpl(agentService, definitionService, assetIndex);
        context.registerService(ContractDefinitionService.class, definitionService);

        // Register the created contract offer service with the service extension context.
        context.registerService(ContractOfferService.class, contractOfferService);

        RemoteMessageDispatcherRegistry dispatcherRegistry = context.getService(RemoteMessageDispatcherRegistry.class, true);
        if (dispatcherRegistry == null) {
            dispatcherRegistry = new RemoteMessageDispatcherRegistryImpl();
            context.registerService(RemoteMessageDispatcherRegistry.class, dispatcherRegistry);
        }

        // negotiation
        var validationService = new ContractValidationServiceImpl(agentService, () -> definitionService, assetIndex);
        context.registerService(ContractValidationService.class, validationService);

        var waitStrategy = context.hasService(NegotiationWaitStrategy.class) ? context.getService(NegotiationWaitStrategy.class) : new ExponentialWaitStrategy(DEFAULT_ITERATION_WAIT);

        consumerNegotiationManager = ConsumerContractNegotiationManagerImpl.Builder.newInstance()
                .waitStrategy(waitStrategy)
                .dispatcherRegistry(dispatcherRegistry)
                .monitor(monitor)
                .validationService(validationService)
                .build();

        providerNegotiationManager = ProviderContractNegotiationManagerImpl.Builder.newInstance()
                .waitStrategy(waitStrategy)
                .dispatcherRegistry(dispatcherRegistry)
                .monitor(monitor)
                .validationService(validationService)
                .build();

        context.registerService(ConsumerContractNegotiationManager.class, consumerNegotiationManager);
        context.registerService(ProviderContractNegotiationManager.class, providerNegotiationManager);
    }

    private void registerTypes(ServiceExtensionContext context) {
        var typeManager = context.getTypeManager();
        typeManager.registerTypes(ContractNegotiation.class);
    }

}
