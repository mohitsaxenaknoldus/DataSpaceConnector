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

function lease(processId, connectorId, shouldLease) {
    var context = getContext();
    var collection = context.getCollection();
    var collectionLink = collection.getSelfLink();
    var response = context.getResponse();


    // first query
    var filterQuery = {
        'query': 'SELECT * FROM ContractNegotiationDocument t WHERE t.id = @negotiationId',
        'parameters': [
            {
                'name': '@negotiationId', 'value': processId
            }
        ]
    };

    var accept = collection.queryDocuments(collectionLink, filterQuery, {}, function (err, items, responseOptions) {
        if (err) throw new Error("Error" + err.message);


        if (!items || !items.length) {
            let err = "No documents found!";
            response.setBody(err)
            console.log(err)
            return;
        }
        if (items.length > 1) {
            let err = "too many docs found for query: expected 1, got " + items.length;
            console.log(err);
            throw err;
        }

        let document = items[0];

        if (document.lease != null && document.lease.leasedBy !== connectorId) {
            throw "Document is locked by another connector"
        }

        if (shouldLease)
            lease(document, connectorId);
        else //clear lease
            document.lease = null;


        response.setBody(document)
    });

    if (!accept) throw "Unable to read document details, abort ";

    function lease(document, connectorId) {
        document.lease = {
            leasedBy: connectorId,
            leasedAt: Date.now(),
            leaseDuration: 60
        };

        var accept = collection.replaceDocument(document._self, document, function (err, itemReplaced) {
            if (err) throw "Unable to update Document, abort ";
        })
        if (!accept) throw "Unable to update Document, abort";
        console.log("updated lease of document " + document.id)
    }
}