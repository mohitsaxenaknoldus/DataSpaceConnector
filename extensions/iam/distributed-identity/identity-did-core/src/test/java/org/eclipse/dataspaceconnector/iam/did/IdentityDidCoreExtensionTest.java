package org.eclipse.dataspaceconnector.iam.did;

import okhttp3.OkHttpClient;
import org.eclipse.dataspaceconnector.iam.did.hub.IdentityHubClientImpl;
import org.eclipse.dataspaceconnector.iam.did.hub.IdentityHubController;
import org.eclipse.dataspaceconnector.iam.did.hub.IdentityHubImpl;
import org.eclipse.dataspaceconnector.iam.did.resolution.DefaultDidPublicKeyResolver;
import org.eclipse.dataspaceconnector.iam.did.spi.hub.IdentityHub;
import org.eclipse.dataspaceconnector.iam.did.spi.hub.IdentityHubClient;
import org.eclipse.dataspaceconnector.iam.did.spi.hub.IdentityHubStore;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidPublicKeyResolver;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.protocol.web.WebService;
import org.eclipse.dataspaceconnector.spi.security.PrivateKeyResolver;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.types.TypeManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class IdentityDidCoreExtensionTest {

    private IdentityDidCoreExtension extension;
    private ServiceExtensionContext contextMock;

    @BeforeEach
    void setUp() {
        extension = new IdentityDidCoreExtension();
        contextMock = mock(ServiceExtensionContext.class);
    }

    @Test
    void verifyCorrectInitialization_withPkResolverPresent() {
        when(contextMock.getService(IdentityHubStore.class)).thenReturn(mock(IdentityHubStore.class));
        when(contextMock.getTypeManager()).thenReturn(new TypeManager());
        when(contextMock.getService(PrivateKeyResolver.class)).thenReturn(mock(PrivateKeyResolver.class));
        when(contextMock.getConnectorId()).thenReturn("test-connector");
        WebService webserviceMock = mock(WebService.class);
        when(contextMock.getService(WebService.class)).thenReturn(webserviceMock);
        when(contextMock.getService(OkHttpClient.class)).thenReturn(mock(OkHttpClient.class));
        when(contextMock.getMonitor()).thenReturn(mock(Monitor.class));

        extension.initialize(contextMock);

        verify(contextMock).registerService(eq(DidResolverRegistry.class), isA(DidResolverRegistry.class));
        verify(contextMock).registerService(eq(DidPublicKeyResolver.class), isA(DefaultDidPublicKeyResolver.class));
        verify(contextMock).registerService(eq(IdentityHub.class), isA(IdentityHubImpl.class));
        verify(contextMock).registerService(eq(IdentityHubClient.class), isA(IdentityHubClientImpl.class));
        verify(webserviceMock).registerController(isA(IdentityHubController.class));
    }
}
