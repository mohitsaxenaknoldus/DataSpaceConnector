package org.eclipse.dataspaceconnector.extensions.api;

import org.eclipse.dataspaceconnector.spi.asset.DataAddressResolver;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.transfer.flow.DataFlowController;
import org.eclipse.dataspaceconnector.spi.transfer.flow.DataFlowInitiateResult;
import org.eclipse.dataspaceconnector.spi.transfer.response.ResponseStatus;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileTransferFlowController implements DataFlowController {
    private final Monitor monitor;
    private final DataAddressResolver dataAddressResolver;

    public FileTransferFlowController(Monitor monitor, DataAddressResolver dataAddressResolver) {
        this.monitor = monitor;
        this.dataAddressResolver = dataAddressResolver;
    }

    @Override
    public boolean canHandle(DataRequest dataRequest) {
        return dataRequest.getDataDestination().getType().equalsIgnoreCase("file");
    }

    @Override
    public @NotNull DataFlowInitiateResult initiateFlow(DataRequest dataRequest) {
        var source = dataAddressResolver.resolveForAsset(dataRequest.getAssetId());
        var destination = dataRequest.getDataDestination();

        // verify source path
        String sourceFileName = source.getProperty("filename");
        var sourcePath = Path.of(source.getProperty("path"), sourceFileName);
        if (!sourcePath.toFile().exists()) {
            return DataFlowInitiateResult.failure(ResponseStatus.FATAL_ERROR, "source file " + sourcePath + " does not exist!");
        }

        // verify destination path
        var destinationPath = Path.of(destination.getProperty("path"));
        if (!destinationPath.toFile().exists()) { //interpret as directory
            monitor.debug("Destination path " + destinationPath + " does not exist, will attempt to create");
            try {
                Files.createDirectory(destinationPath);
                monitor.debug("Successfully created destination path " + destinationPath);
            } catch (IOException e) {
                String message = "Error creating directory: " + e.getMessage();
                monitor.severe(message);
                return DataFlowInitiateResult.failure(ResponseStatus.FATAL_ERROR, message);
            }
        } else if (destinationPath.toFile().isDirectory()) {
            destinationPath = Path.of(destinationPath.toString(), sourceFileName);
        }

        try {
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            monitor.info("Successfully copied file to " + destinationPath);
        } catch (IOException e) {
            String message = "Error copying file " + e.getMessage();
            monitor.severe(message);
            return DataFlowInitiateResult.failure(ResponseStatus.FATAL_ERROR, message);

        }

        return DataFlowInitiateResult.success("");
    }

}
