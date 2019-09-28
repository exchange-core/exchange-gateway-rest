package org.openpredict.exchange.rest.model.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.openpredict.exchange.rest.events.MatchingRole;

import java.math.BigDecimal;

@AllArgsConstructor
@Builder
@Getter
@ToString
public class GatewayDeal {

    private final long size;
    private final BigDecimal price;
    private final MatchingRole matchingRole;
    private final long timestamp;

    // hidden from regular user
    private final long counterOrderId;
    private final long counterPartyUid;
}
