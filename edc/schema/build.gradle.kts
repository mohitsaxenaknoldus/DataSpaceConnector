/*
 * Copyright (c) Microsoft Corporation.
 * All rights reserved.
 */

plugins {
    `java-library`
}

dependencies {
    api(project(":edc:spi"))
}

publishing {
    publications {
        create<MavenPublication>("schema") {
            artifactId = "edc.schema"
            from(components["java"])
        }
    }
}